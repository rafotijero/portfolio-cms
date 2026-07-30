package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.Experience;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ExperienceDto(
        UUID id,
        String role,
        String company,
        LocalDate startDate,
        LocalDate endDate,
        String summary,
        List<String> techStack,
        String logoUrl
) {

    public static ExperienceDto from(Experience experience) {
        return new ExperienceDto(
                experience.getId(),
                experience.getRole(),
                experience.getCompany(),
                experience.getStartDate(),
                experience.getEndDate(),
                experience.getSummary(),
                experience.getTechStack(),
                experience.getLogoUrl()
        );
    }
}
