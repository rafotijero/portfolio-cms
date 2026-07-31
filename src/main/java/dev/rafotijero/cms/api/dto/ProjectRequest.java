package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProjectRequest(
        @NotBlank String name,
        @NotBlank String slug,
        @NotNull ContentStatus status,
        String description,
        List<String> techStack,
        String repoUrl,
        String liveUrl,
        String coverImageUrl,
        Boolean featured,
        Integer displayOrder
) {
}
