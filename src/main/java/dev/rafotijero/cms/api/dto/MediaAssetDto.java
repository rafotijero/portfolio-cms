package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.MediaAsset;

import java.time.Instant;
import java.util.UUID;

public record MediaAssetDto(
        UUID id, String filename, String url, String contentType, Long sizeBytes, Instant uploadedAt) {

    public static MediaAssetDto from(MediaAsset asset) {
        return new MediaAssetDto(
                asset.getId(),
                asset.getFilename(),
                asset.getUrl(),
                asset.getContentType(),
                asset.getSizeBytes(),
                asset.getUploadedAt());
    }
}
