package dev.rafotijero.cms.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AboutParagraphRequest(
        @NotBlank String content,
        Integer displayOrder
) {
}
