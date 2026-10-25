package com.oliviya.tasktracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.oliviya.tasktracker.entity.Project;
import com.oliviya.tasktracker.exception.APIException;
import com.oliviya.tasktracker.exception.ResourceNotFoundException;
import com.oliviya.tasktracker.payload.ProjectDTO;
import com.oliviya.tasktracker.payload.ProjectResponse;
import com.oliviya.tasktracker.repository.ProjectRepository;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceImplTest {
    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;
    private Project project;
    private ProjectDTO projectDTO;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setProjectId(1L);
        project.setProjectName("Platform Migration");
        project.setProjectDescription("Move services to GCP");
        projectDTO = new ProjectDTO(1L, "Platform Migration", "Move services to GCP");
    }

    // create project

    @Test
    @DisplayName("createProject saves and returns the project when the name is unique")
    void createProject_success() {
        ProjectDTO request = new ProjectDTO(null, "Platform Migration", "Move services o GCP");
        Project mapped = new Project();
        mapped.setProjectName("Platform Migration");
        mapped.setProjectDescription("Move services to GCP");

        when(modelMapper.map(request, Project.class)).thenReturn(mapped);
        when(projectRepository.findByProjectName("Platform Migration")).thenReturn(null);
        when(projectRepository.save(mapped)).thenReturn(project);
        when(modelMapper.map(project, ProjectDTO.class)).thenReturn(projectDTO);

        ProjectDTO result = projectService.createProject(request);
        assertEquals(1L, result.getProjectId());
        assertEquals("Move services to GCP", result.getProjectDescription());
        verify(projectRepository).save(mapped);
    }

    @Test
    @DisplayName("createProject throws APIException and does not save when the name already exist")
    void createProject_duplicateName_throws() {
        Project mapped = new Project();
        mapped.setProjectName("Platform Migration");
        when(modelMapper.map(projectDTO, Project.class)).thenReturn(mapped);
        when(projectRepository.findByProjectName("Platform Migration")).thenReturn(project);

        APIException ex = assertThrows(APIException.class, () -> projectService.createProject(projectDTO));

        assertTrue(ex.getMessage().contains("Platform Migration"));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(projectRepository, never()).save(any(Project.class));
    }

    // get project by id

    @Test
    @DisplayName("getProjectById returns the mapped DTO when the project exists")
    void getProjectById_success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(modelMapper.map(project, ProjectDTO.class)).thenReturn(projectDTO);
        ProjectDTO result = projectService.getProjectById(1L);
        assertEquals(projectDTO, result);
        verify(projectRepository).findById(1L);
    }

    @Test
    @DisplayName("getProjectId throws ResourceNotFoundException when the project is missing")
    void getProjectById_notFound_throws() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> projectService.getProjectById(99L));
        assertEquals("Project not found with id: 99", ex.getMessage());
        verifyNoInteractions(modelMapper);
    }

    // update project

    @Test
    @DisplayName("updateProject saves the mapped project under the path id")
    void updateProject_success() {
        ProjectDTO request = new ProjectDTO(null, "Renamed", "New description");
        Project mapped = new Project();
        mapped.setProjectName("Renamed");
        mapped.setProjectDescription("New description");

        Project saved = new Project();
        saved.setProjectId(1L);
        saved.setProjectName("Renamed");
        saved.setProjectDescription("New description");
        ProjectDTO expected = new ProjectDTO(1L, "Renamed", "New description");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(modelMapper.map(request, Project.class)).thenReturn(mapped);
        when(projectRepository.save(mapped)).thenReturn(saved);
        when(modelMapper.map(saved, ProjectDTO.class)).thenReturn(expected);

        ProjectDTO result = projectService.updateProject(request, 1L);

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getProjectId());
        assertEquals("Renamed", result.getProjectName());
        assertEquals("New description", result.getProjectDescription());
    }

    @Test
    @DisplayName("updateProject throws ResourceNotFoundException and does not save when the project is missing")
    void updateProject_notFound_throws() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject(projectDTO, 99L));

        verify(projectRepository, never()).save(any(Project.class));
    }

    // delete project

    @Test
    @DisplayName("deleteProject deletes the entity and returns its DTO")
    void deleteProject_success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(modelMapper.map(project, ProjectDTO.class)).thenReturn(projectDTO);

        ProjectDTO result = projectService.deleteProject(1L);

        assertEquals(projectDTO, result);
        verify(projectRepository).delete(project);
    }

    @Test
    @DisplayName("deleteProject throws ResourceNotFoundException and does not delete when the project is missing")
    void deleteProject_notFound_throws() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProject(99L));

        verify(projectRepository, never()).delete(any(Project.class));
    }

    // get all projects

    @Test
    @DisplayName("getAllProjects returns paging metadata and sorts ascending")
    void getAllProjects_ascending_success() {
        Page<Project> page = new PageImpl<>(List.of(project), PageRequest.of(0, 10), 1);
        when(projectRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(project, ProjectDTO.class)).thenReturn(projectDTO);

        ProjectResponse response = projectService.getAllProjects(0, 10, "projectName", "asc");

        assertEquals(1, response.getContent().size());
        assertEquals(projectDTO, response.getContent().get(0));
        assertEquals(0, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLastPage());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(projectRepository).findAll(captor.capture());
        Sort.Order order = captor.getValue().getSort().getOrderFor("projectName");
        assertNotNull(order);
        assertTrue(order.isAscending());
    }

    @Test
    @DisplayName("getAllProjects sorts descending when sortOrder is not asc")
    void getAllProjects_descending_success() {
        Page<Project> page = new PageImpl<>(List.of(project), PageRequest.of(0, 10), 1);
        when(projectRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(project, ProjectDTO.class)).thenReturn(projectDTO);

        projectService.getAllProjects(0, 10, "projectName", "desc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(projectRepository).findAll(captor.capture());
        Sort.Order order = captor.getValue().getSort().getOrderFor("projectName");
        assertNotNull(order);
        assertTrue(order.isDescending());
    }

    @Test
    @DisplayName("getAllProjects throws APIException when there are no projects (current behaviour)")
    void getAllProjects_empty_throws() {
        Page<Project> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(projectRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        APIException ex = assertThrows(APIException.class,
                () -> projectService.getAllProjects(0, 10, "projectName", "asc"));

        assertEquals("No projects created till now.", ex.getMessage());
        verifyNoInteractions(modelMapper);
    }
}
