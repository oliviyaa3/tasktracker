package com.oliviya.tasktracker.service;

import java.util.List;

import com.oliviya.tasktracker.payload.TaskDTO;
import com.oliviya.tasktracker.payload.TaskResponse;

public interface TaskService {

    TaskDTO createTask(TaskDTO taskDTO);

    TaskDTO getTaskById(Long id);

    TaskDTO updateTask(TaskDTO taskDTO, Long id);

    TaskDTO deleteTask(Long id);

    TaskResponse getAllTasks(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    List<TaskDTO> getTasksByProject(Long projectId);
}
