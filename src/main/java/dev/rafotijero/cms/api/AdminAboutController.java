package dev.rafotijero.cms.api;

import dev.rafotijero.cms.api.dto.AboutParagraphDto;
import dev.rafotijero.cms.api.dto.AboutParagraphRequest;
import dev.rafotijero.cms.service.AboutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/about")
public class AdminAboutController {

    private final AboutService aboutService;

    public AdminAboutController(AboutService aboutService) {
        this.aboutService = aboutService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AboutParagraphDto create(@Valid @RequestBody AboutParagraphRequest request) {
        return aboutService.create(request);
    }

    @PutMapping("/{id}")
    public AboutParagraphDto update(@PathVariable UUID id, @Valid @RequestBody AboutParagraphRequest request) {
        return aboutService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        aboutService.delete(id);
    }
}
