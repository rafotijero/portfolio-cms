package dev.rafotijero.cms.api;

import dev.rafotijero.cms.api.dto.SkillGroupDto;
import dev.rafotijero.cms.service.SkillService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public List<SkillGroupDto> list() {
        return skillService.findAllGrouped();
    }
}
