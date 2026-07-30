package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.ProjectDto;
import dev.rafotijero.cms.domain.ContentStatus;
import dev.rafotijero.cms.domain.Project;
import dev.rafotijero.cms.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
}
