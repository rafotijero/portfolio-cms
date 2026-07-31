package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.AboutParagraph;

import java.util.UUID;

public record AboutParagraphDto(UUID id, String content, Integer displayOrder) {

    public static AboutParagraphDto from(AboutParagraph paragraph) {
        return new AboutParagraphDto(paragraph.getId(), paragraph.getContent(), paragraph.getDisplayOrder());
    }
}
