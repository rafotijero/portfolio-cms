package dev.rafotijero.cms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ExperienceRequest(
        @NotBlank String role,
        @NotBlank String company,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotBlank String summary,
        List<String> techStack,
        String logoUrl,
        Integer displayOrder
) {
}
