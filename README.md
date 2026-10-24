# Task Tracker API

A RESTful Spring Boot service for project and task management. Users can be assigned to tasks, and every task belongs to exactly one project.

## Tech Stack

- Java 21 (use the version your `pom.xml` targets)
- Spring Boot (Web, Data JPA, Validation)
- H2 in-memory database
- ModelMapper for entity/DTO mapping
- JUnit 5 and Mockito for unit tests
- springdoc-openapi for Swagger UI

## Prerequisites

- JDK 21 (`java -version`)
- Maven 3.8+ (or use the included `./mvnw` wrapper)
- `curl` (or Postman) for trying the endpoints

No external database is needed. H2 runs inside the application.

## Setup

```bash
git clone <your-repo-url>
cd tasktracker
./mvnw clean install
```

## Run

```bash
./mvnw spring-boot:run
```

The app starts on **http://localhost:8087**.

Run the unit tests only:

```bash
./mvnw test
```

## Useful URLs

| URL                                    | Description          |
| -------------------------------------- | -------------------- |
| http://localhost:8087/swagger-ui.html  | Swagger UI           |
| http://localhost:8087/v3/api-docs.yaml | OpenAPI spec (YAML)  |
| http://localhost:8087/h2-console       | H2 console           |
| http://localhost:8087/api/info         | App name and version |

H2 console login:

| Field     | Value                       |
| --------- | --------------------------- |
| JDBC URL  | `jdbc:h2:mem:tasktrackerdb` |
| User Name | `sa`                        |
| Password  |                             |

## Configuration

Configuration is in `src/main/resources/application.yaml`.

## Behaviour Notes

- A task must be created with a valid `projectId`. An unknown project or assignee returns `404`.
- If `taskStatus` is omitted on create, it defaults to `TODO`. If `taskPriority` is omitted, it defaults to `task.default-priority`.
- On update (PUT), `taskStatus`, `taskPriority`, `projectId` and `assigneeId` are only changed when supplied.
- Deleting a project also deletes its tasks.
- Deleting a user unassigns that user's tasks (assignee becomes null).
- Project names and user emails must be unique.
- The H2 database is in memory, so data is lost on every restart.

## Error Response Format

Errors return a structured JSON body with an appropriate HTTP status:

| Status | When                                                                             |
| ------ | -------------------------------------------------------------------------------- |
| 400    | Validation failure or malformed request body (for example an invalid enum value) |
| 404    | Resource not found                                                               |
| 409    | Duplicate name or email                                                          |
| 500    | Unexpected server error                                                          |

## Sample curl Requests

The database starts empty, so create a project and a user first. The ids below assume a fresh start (both `1`).

### App info

```bash
curl http://localhost:8087/api/info
```

### Projects

```bash
# Create
curl -X POST http://localhost:8087/api/projects \
  -H "Content-Type: application/json" \
  -d '{"projectName": "Platform Migration", "projectDescription": "Move services to GCP"}'

# List (paginated)
curl "http://localhost:8087/api/projects?pageNumber=0&pageSize=10&sortBy=projectId&sortOrder=asc"

# Get by id
curl http://localhost:8087/api/projects/1

# Update
curl -X PUT http://localhost:8087/api/projects/1 \
  -H "Content-Type: application/json" \
  -d '{"projectName": "Platform Migration", "projectDescription": "Updated description"}'

# Delete
curl -X DELETE http://localhost:8087/api/projects/1
```

### Users

```bash
# Create
curl -X POST http://localhost:8087/api/users \
  -H "Content-Type: application/json" \
  -d '{"userName": "Oliviya", "userEmail": "oliviya@example.com", "userRole": "DEVELOPER"}'

# List
curl http://localhost:8087/api/users

# Get by id
curl http://localhost:8087/api/users/1

# Update
curl -X PUT http://localhost:8087/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"userName": "Oliviya", "userEmail": "oliviya@example.com", "userRole": "MANAGER"}'

# Delete
curl -X DELETE http://localhost:8087/api/users/1
```

### Tasks

```bash
# Create a task under project 1, assigned to user 1
curl -X POST http://localhost:8087/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
        "taskTitle": "Create a CI pipeline",
        "taskStatus": "TODO",
        "taskPriority": "HIGH",
        "projectId": 1,
        "assigneeId": 1,
        "dueDate": "2026-11-15"
      }'

# Create with defaults (status TODO, priority from config, no assignee)
curl -X POST http://localhost:8087/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"taskTitle": "Write documentation", "projectId": 1}'

# List all tasks
curl "http://localhost:8087/api/tasks?pageNumber=0&pageSize=10&sortBy=id&sortOrder=asc"

# Get by id
curl http://localhost:8087/api/tasks/1

# Update
curl -X PUT http://localhost:8087/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
        "taskTitle": "Create a CI pipeline using Cloud Build",
        "taskStatus": "IN_PROGRESS",
        "taskPriority": "HIGH",
        "projectId": 1,
        "assigneeId": 1,
        "dueDate": "2026-11-20"
      }'

# Delete
curl -X DELETE http://localhost:8087/api/tasks/1

# All tasks for a project
curl http://localhost:8087/api/projects/1/tasks
```

### Error examples

```bash
# 400: missing required projectId
curl -X POST http://localhost:8087/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"taskTitle": "No project"}'

# 404: project does not exist
curl -X POST http://localhost:8087/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"taskTitle": "Orphan", "projectId": 999}'

# 400: invalid enum value
curl -X POST http://localhost:8087/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"taskTitle": "Bad status", "taskStatus": "DONEE", "projectId": 1}'
```

## Project Structure

```
src/main/java/com/oliviya/tasktracker
├── config        # AppConstants, ModelMapper and OpenAPI beans
├── controller    # REST controllers
├── entity        # JPA entities
├── enums         # Status, Priority
├── exception     # Custom exceptions and global handler
├── payload       # DTOs
├── repository    # Spring Data repositories
└── service       # Business logic
```
