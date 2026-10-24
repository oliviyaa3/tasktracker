package com.oliviya.tasktracker.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Long projectId;
    @NotBlank(message = "Project name is required")
    private String projectName;
    private String projectDescription;
}