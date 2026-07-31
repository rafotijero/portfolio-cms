package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.Tag;

import java.util.UUID;

public record TagDto(UUID id, String name, String slug) {

    public static TagDto from(Tag tag) {
        return new TagDto(tag.getId(), tag.getName(), tag.getSlug());
    }
}
