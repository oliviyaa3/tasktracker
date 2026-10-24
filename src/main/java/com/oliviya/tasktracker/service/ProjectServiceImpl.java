package com.oliviya.tasktracker.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.oliviya.tasktracker.entity.Project;
import com.oliviya.tasktracker.exception.APIException;
import com.oliviya.tasktracker.exception.ResourceNotFoundException;
import com.oliviya.tasktracker.payload.ProjectDTO;
import com.oliviya.tasktracker.payload.ProjectResponse;
import com.oliviya.tasktracker.repository.ProjectRepository;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ModelMapper modelMapper;

    ProjectServiceImpl(ProjectRepository projectRepository, ModelMapper modelMapper) {
        this.projectRepository = projectRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ProjectResponse getAllProjects(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Project> projectPage = projectRepository.findAll(pageDetails);

        List<Project> projects = projectPage.getContent();
        if (projects.isEmpty())
            throw new APIException("No projects created till now.");

        List<ProjectDTO> projectDTOS = projects.stream()
                .map(project -> modelMapper.map(project, ProjectDTO.class))
                .toList();

        ProjectResponse projectResponse = new ProjectResponse();
        projectResponse.setContent(projectDTOS);
        projectResponse.setPageNumber(projectPage.getNumber());
        projectResponse.setPageSize(projectPage.getSize());
        projectResponse.setTotalElements(projectPage.getTotalElements());
        projectResponse.setTotalPages(projectPage.getTotalPages());
        projectResponse.setLastPage(projectPage.isLast());
        return projectResponse;
    }

    @Override
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        Project project = modelMapper.map(projectDTO, Project.class);
        Project projectFromDb = projectRepository.findByProjectName(project.getProjectName());
        if (projectFromDb != null)
            throw new APIException("Project with the name " + project.getProjectName() + " already exists !!!");
        Project savedProject = projectRepository.save(project);
        return modelMapper.map(savedProject, ProjectDTO.class);
    }

    @Override
    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return modelMapper.map(project, ProjectDTO.class);
    }

    @Override
    public ProjectDTO updateProject(ProjectDTO projectDTO, Long id) {
        Project savedProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        Project updatedProject = modelMapper.map(projectDTO, Project.class);
        updatedProject.setProjectId(id);
        savedProject = projectRepository.save(updatedProject);
        return modelMapper.map(savedProject, ProjectDTO.class);
    }

    @Override
    public ProjectDTO deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        projectRepository.delete(project);
        return modelMapper.map(project, ProjectDTO.class);
    }

}