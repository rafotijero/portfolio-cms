package dev.rafotijero.cms.api;

import dev.rafotijero.cms.api.dto.SiteProfileDto;
import dev.rafotijero.cms.service.SiteProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/site")
public class SiteProfileController {

    private final SiteProfileService siteProfileService;

    public SiteProfileController(SiteProfileService siteProfileService) {
        this.siteProfileService = siteProfileService;
    }

    @GetMapping
    public SiteProfileDto get() {
        return siteProfileService.find();
    }
}
