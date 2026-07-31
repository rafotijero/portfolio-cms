package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.ContentStatus;
import dev.rafotijero.cms.domain.Project;

import java.util.List;
import java.util.UUID;

public record ProjectDto(
        UUID id,
        String name,
        String slug,
        ContentStatus status,
        String description,
        List<String> techStack,
        String repoUrl,
        String liveUrl,
        String coverImageUrl,
        boolean featured,
        Integer displayOrder
) {

    public static ProjectDto from(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getName(),
                project.getSlug(),
                project.getStatus(),
                project.getDescription(),
                project.getTechStack(),
                project.getRepoUrl(),
                project.getLiveUrl(),
                project.getCoverImageUrl(),
                project.isFeatured(),
                project.getDisplayOrder()
        );
    }
}
