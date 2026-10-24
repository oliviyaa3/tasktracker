package com.oliviya.tasktracker.service;

import com.oliviya.tasktracker.payload.ProjectDTO;
import com.oliviya.tasktracker.payload.ProjectResponse;

public interface ProjectService {

    ProjectResponse getAllProjects(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProjectDTO createProject(ProjectDTO projectDTO);

    ProjectDTO getProjectById(Long id);

    ProjectDTO updateProject(ProjectDTO projectDTO, Long id);

    ProjectDTO deleteProject(Long id);

}
