package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.SiteProfileDto;
import dev.rafotijero.cms.api.dto.SiteProfileRequest;
import dev.rafotijero.cms.domain.SiteProfile;
import dev.rafotijero.cms.repository.SiteProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class SiteProfileService {

    private final SiteProfileRepository siteProfileRepository;

    public SiteProfileService(SiteProfileRepository siteProfileRepository) {
        this.siteProfileRepository = siteProfileRepository;
    }

    public SiteProfileDto find() {
        return siteProfileRepository.findById(Boolean.TRUE)
                .map(SiteProfileDto::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public SiteProfileDto save(SiteProfileRequest request) {
        SiteProfile siteProfile = siteProfileRepository.findById(Boolean.TRUE).orElseGet(SiteProfile::new);

        siteProfile.setName(request.name());
        siteProfile.setRole(request.role());
        siteProfile.setTagline(request.tagline());
        siteProfile.setLocation(request.location());
        siteProfile.setEmail(request.email());
        siteProfile.setGithubUrl(request.githubUrl());
        siteProfile.setLinkedinUrl(request.linkedinUrl());
        siteProfile.setCvUrl(request.cvUrl());
        siteProfile.setCip(request.cip());

        return SiteProfileDto.from(siteProfileRepository.save(siteProfile));
    }
}
