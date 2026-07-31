package dev.rafotijero.cms.api.dto;

import jakarta.validation.constraints.NotBlank;

public record SkillRequest(
        @NotBlank String skillGroup,
        @NotBlank String name,
        String icon,
        Integer displayOrder
) {
}
