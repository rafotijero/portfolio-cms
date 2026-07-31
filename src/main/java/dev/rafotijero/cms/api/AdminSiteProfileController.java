package dev.rafotijero.cms.api;

import dev.rafotijero.cms.api.dto.SiteProfileDto;
import dev.rafotijero.cms.api.dto.SiteProfileRequest;
import dev.rafotijero.cms.service.SiteProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/site")
public class AdminSiteProfileController {

    private final SiteProfileService siteProfileService;

    public AdminSiteProfileController(SiteProfileService siteProfileService) {
        this.siteProfileService = siteProfileService;
    }

    @PutMapping
    public SiteProfileDto save(@Valid @RequestBody SiteProfileRequest request) {
        return siteProfileService.save(request);
    }
}
