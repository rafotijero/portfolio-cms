package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record PostRequest(
        @NotBlank String title,
        @NotBlank String slug,
        @NotNull ContentStatus status,
        String summary,
        @NotBlank String content,
        String coverImageUrl,
        Instant publishedAt,
        Set<UUID> tagIds
) {
}
