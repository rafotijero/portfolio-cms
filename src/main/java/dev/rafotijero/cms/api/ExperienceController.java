package dev.rafotijero.cms.api;

import dev.rafotijero.cms.api.dto.ExperienceDto;
import dev.rafotijero.cms.service.ExperienceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/experience")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @GetMapping
    public List<ExperienceDto> list() {
        return experienceService.findAll();
    }
}
