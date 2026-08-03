package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.AboutParagraph;

import java.time.Instant;
import java.util.UUID;

public record AboutParagraphDto(
        UUID id,
        String content,
        Integer displayOrder,
        Instant createdAt,
        Instant updatedAt
) {

    public static AboutParagraphDto from(AboutParagraph paragraph) {
        return new AboutParagraphDto(
                paragraph.getId(),
                paragraph.getContent(),
                paragraph.getDisplayOrder(),
                paragraph.getCreatedAt(),
                paragraph.getUpdatedAt()
        );
    }
}
