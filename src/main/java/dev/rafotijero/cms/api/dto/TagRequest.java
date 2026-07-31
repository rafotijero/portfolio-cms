package dev.rafotijero.cms.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
        @NotBlank String name,
        @NotBlank String slug
) {
}
