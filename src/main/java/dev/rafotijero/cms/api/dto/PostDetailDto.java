package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.ContentStatus;
import dev.rafotijero.cms.domain.Post;
import dev.rafotijero.cms.domain.Tag;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostDetailDto(
        UUID id,
        String title,
        String slug,
        ContentStatus status,
        String summary,
        String content,
        String coverImageUrl,
        Instant publishedAt,
        List<String> tags
) {

    public static PostDetailDto from(Post post) {
        return new PostDetailDto(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getStatus(),
                post.getSummary(),
                post.getContent(),
                post.getCoverImageUrl(),
                post.getPublishedAt(),
                post.getTags().stream().map(Tag::getName).toList()
        );
    }
}
