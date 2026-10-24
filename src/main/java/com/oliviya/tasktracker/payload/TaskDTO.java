package com.oliviya.tasktracker.payload;

import java.time.LocalDate;

import com.oliviya.tasktracker.enums.Priority;
import com.oliviya.tasktracker.enums.Status;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long taskId;
    @NotNull(message = "Title is required")
    private String taskTitle;
    private Status taskStatus;
    private Priority taskPriority;
    @NotNull(message = "projectId is required")
    private Long projectId;
    private Long assigneeId;
    private LocalDate dueDate;
}
