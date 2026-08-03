package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.MediaAssetDto;
import dev.rafotijero.cms.domain.MediaAsset;
import dev.rafotijero.cms.repository.MediaAssetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MediaAssetService {

    private final MediaAssetRepository mediaAssetRepository;
    private final S3Client s3Client;
    private final String bucketName;
    private final String publicUrl;

    public MediaAssetService(
            MediaAssetRepository mediaAssetRepository,
            S3Client s3Client,
            @Value("${r2.bucket-name}") String bucketName,
            @Value("${r2.public-url}") String publicUrl) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.publicUrl = publicUrl;
    }

    public List<MediaAssetDto> findAll() {
        return mediaAssetRepository.findAllByOrderByUploadedAtDesc().stream().map(MediaAssetDto::from).toList();
    }

    @Transactional
    public MediaAssetDto upload(MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo esta vacio o no tiene nombre");
        }

        String originalFilename = file.getOriginalFilename();
        String key = "%s-%s".formatted(UUID.randomUUID(), sanitize(originalFilename));

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al leer el archivo", e);
        }

        MediaAsset asset = new MediaAsset();
        asset.setFilename(originalFilename);
        asset.setUrl("%s/%s".formatted(publicUrl, key));
        asset.setContentType(file.getContentType());
        asset.setSizeBytes(file.getSize());

        return MediaAssetDto.from(mediaAssetRepository.saveAndFlush(asset));
    }

    @Transactional
    public void delete(UUID id) {
        MediaAsset asset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String key = asset.getUrl().replace(publicUrl + "/", "");
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());

        mediaAssetRepository.delete(asset);
    }

    private String sanitize(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    }
}
