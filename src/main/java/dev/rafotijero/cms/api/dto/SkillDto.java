package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.Skill;

import java.time.Instant;
import java.util.UUID;

public record SkillDto(
        UUID id,
        String skillGroup,
        String name,
        String icon,
        Integer displayOrder,
        Instant createdAt,
        Instant updatedAt
) {

    public static SkillDto from(Skill skill) {
        return new SkillDto(
                skill.getId(),
                skill.getSkillGroup(),
                skill.getName(),
                skill.getIcon(),
                skill.getDisplayOrder(),
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }
}
