package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.ContentStatus;
import dev.rafotijero.cms.domain.Post;
import dev.rafotijero.cms.domain.Tag;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostSummaryDto(
        UUID id,
        String title,
        String slug,
        ContentStatus status,
        String summary,
        String coverImageUrl,
        Instant publishedAt,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt
) {

    public static PostSummaryDto from(Post post) {
        return new PostSummaryDto(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getStatus(),
                post.getSummary(),
                post.getCoverImageUrl(),
                post.getPublishedAt(),
                post.getTags().stream().map(Tag::getName).toList(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
