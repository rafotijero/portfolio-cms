package dev.rafotijero.cms.api.dto;

import java.util.List;

public record SkillGroupDto(String group, List<SkillItemDto> items) {
}
