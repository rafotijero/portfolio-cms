package dev.rafotijero.cms.api;

import dev.rafotijero.cms.api.dto.ExperienceDto;
import dev.rafotijero.cms.api.dto.ExperienceRequest;
import dev.rafotijero.cms.service.ExperienceService;
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
@RequestMapping("/api/v1/admin/experience")
public class AdminExperienceController {

    private final ExperienceService experienceService;

    public AdminExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExperienceDto create(@Valid @RequestBody ExperienceRequest request) {
        return experienceService.create(request);
    }

    @PutMapping("/{id}")
    public ExperienceDto update(@PathVariable UUID id, @Valid @RequestBody ExperienceRequest request) {
        return experienceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        experienceService.delete(id);
    }
}
