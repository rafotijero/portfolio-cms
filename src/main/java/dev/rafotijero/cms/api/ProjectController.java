package dev.rafotijero.cms.api;

import dev.rafotijero.cms.api.dto.ProjectDto;
import dev.rafotijero.cms.service.ProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectDto> list() {
        return projectService.findPublished();
    }

    @GetMapping("/{slug}")
    public ProjectDto detail(@PathVariable String slug) {
        return projectService.findPublishedBySlug(slug);
    }
}
