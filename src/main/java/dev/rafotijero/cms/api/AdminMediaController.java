package dev.rafotijero.cms.api;

import dev.rafotijero.cms.api.dto.MediaAssetDto;
import dev.rafotijero.cms.service.MediaAssetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/media")
public class AdminMediaController {

    private final MediaAssetService mediaAssetService;

    public AdminMediaController(MediaAssetService mediaAssetService) {
        this.mediaAssetService = mediaAssetService;
    }

    @GetMapping
    public List<MediaAssetDto> list() {
        return mediaAssetService.findAll();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaAssetDto upload(@RequestParam("file") MultipartFile file) {
        return mediaAssetService.upload(file);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        mediaAssetService.delete(id);
    }
}
