package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.ProjectDto;
import dev.rafotijero.cms.api.dto.ProjectRequest;
import dev.rafotijero.cms.domain.ContentStatus;
import dev.rafotijero.cms.domain.Project;
import dev.rafotijero.cms.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<ProjectDto> findPublished() {
        return projectRepository.findByStatusOrderByDisplayOrderAsc(ContentStatus.PUBLISHED).stream()
                .map(ProjectDto::from)
                .toList();
    }

    public ProjectDto findPublishedBySlug(String slug) {
        Project project = projectRepository.findBySlug(slug)
                .filter(p -> p.getStatus() == ContentStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ProjectDto.from(project);
    }

    public List<ProjectDto> findAllAdmin() {
        return projectRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(ProjectDto::from)
                .toList();
    }

    public ProjectDto findByIdAdmin(UUID id) {
        return ProjectDto.from(getOrThrow(id));
    }

    @Transactional
    public ProjectDto create(ProjectRequest request) {
        Project project = new Project();
        apply(project, request);
        return ProjectDto.from(projectRepository.save(project));
    }

    @Transactional
    public ProjectDto update(UUID id, ProjectRequest request) {
        Project project = getOrThrow(id);
        apply(project, request);
        return ProjectDto.from(projectRepository.save(project));
    }

    @Transactional
    public void delete(UUID id) {
        Project project = getOrThrow(id);
        projectRepository.delete(project);
    }

    private Project getOrThrow(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void apply(Project project, ProjectRequest request) {
        project.setName(request.name());
        project.setSlug(request.slug());
        project.setStatus(request.status());
        project.setDescription(request.description());
        project.setTechStack(request.techStack() != null ? request.techStack() : new ArrayList<>());
        project.setRepoUrl(request.repoUrl());
        project.setLiveUrl(request.liveUrl());
        project.setCoverImageUrl(request.coverImageUrl());
        project.setFeatured(request.featured() != null ? request.featured() : false);
        project.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
    }
}
