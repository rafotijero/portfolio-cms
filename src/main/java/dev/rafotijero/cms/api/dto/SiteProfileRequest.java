package dev.rafotijero.cms.api.dto;

import jakarta.validation.constraints.NotBlank;

public record SiteProfileRequest(
        @NotBlank String name,
        @NotBlank String role,
        @NotBlank String tagline,
        String location,
        @NotBlank String email,
        String githubUrl,
        String linkedinUrl,
        String cvUrl,
        String cip
) {
}
