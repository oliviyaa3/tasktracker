package com.oliviya.tasktracker.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oliviya.tasktracker.entity.Project;
import com.oliviya.tasktracker.entity.Task;
import com.oliviya.tasktracker.entity.User;
import com.oliviya.tasktracker.enums.Priority;
import com.oliviya.tasktracker.enums.Status;
import com.oliviya.tasktracker.exception.APIException;
import com.oliviya.tasktracker.exception.ResourceNotFoundException;
import com.oliviya.tasktracker.payload.TaskDTO;
import com.oliviya.tasktracker.payload.TaskResponse;
import com.oliviya.tasktracker.repository.ProjectRepository;
import com.oliviya.tasktracker.repository.TaskRepository;
import com.oliviya.tasktracker.repository.UserRepository;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Value("${task.default-priority}")
    private Priority defaultPriority;

    public TaskServiceImpl(TaskRepository taskRepository, ModelMapper modelMapper,
            ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.modelMapper = modelMapper;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public TaskDTO createTask(TaskDTO dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", dto.getProjectId()));

        Task task = new Task();
        task.setTaskTitle(dto.getTaskTitle());
        task.setTaskStatus(dto.getTaskStatus() != null ? dto.getTaskStatus() : Status.TODO);
        task.setTaskPriority(dto.getTaskPriority() != null ? dto.getTaskPriority() : defaultPriority);
        task.setTaskDueDate(dto.getDueDate());
        task.setProject(project);

        if (dto.getAssigneeId() != null) {
            User assignee = userRepository.findById(dto.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getAssigneeId()));
            task.setAssignee(assignee);
        }
        Task createTask = taskRepository.save(task);
        return modelMapper.map(createTask, TaskDTO.class);
    }

    @Override
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return modelMapper.map(task, TaskDTO.class);
    }

    @Override
    public TaskDTO updateTask(TaskDTO taskDTO, Long id) {
        Task updatedTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        Task task = modelMapper.map(taskDTO, Task.class);
        task.setTaskId(id);
        if (taskDTO.getAssigneeId() != null) {
            task.setAssignee(userRepository.findById(taskDTO.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", taskDTO.getAssigneeId())));
        }
        updatedTask = taskRepository.save(task);
        return modelMapper.map(updatedTask, TaskDTO.class);
    }

    @Override
    public TaskDTO deleteTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        taskRepository.delete(task);
        return modelMapper.map(task, TaskDTO.class);
    }

    @Override
    public TaskResponse getAllTasks(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Task> taskPage = taskRepository.findAll(pageDetails);

        List<Task> tasks = taskPage.getContent();
        if (tasks.isEmpty())
            throw new APIException("No tasks created till now.");

        List<TaskDTO> taskDTOS = tasks.stream()
                .map(task -> modelMapper.map(task, TaskDTO.class))
                .toList();

        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setContent(taskDTOS);
        taskResponse.setPageNumber(taskPage.getNumber());
        taskResponse.setPageSize(taskPage.getSize());
        taskResponse.setTotalElements(taskPage.getTotalElements());
        taskResponse.setTotalPages(taskPage.getTotalPages());
        taskResponse.setLastPage(taskPage.isLast());
        return taskResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return taskRepository.findByProject_ProjectId(projectId).stream()
                .map((task) -> modelMapper.map(task, TaskDTO.class))
                .toList();
    }
}
