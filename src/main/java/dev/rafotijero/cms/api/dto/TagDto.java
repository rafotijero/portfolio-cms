package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.Tag;

import java.time.Instant;
import java.util.UUID;

public record TagDto(UUID id, String name, String slug, Instant createdAt, Instant updatedAt) {

    public static TagDto from(Tag tag) {
        return new TagDto(tag.getId(), tag.getName(), tag.getSlug(), tag.getCreatedAt(), tag.getUpdatedAt());
    }
}
