package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.SiteProfile;

public record SiteProfileDto(
        String name,
        String role,
        String tagline,
        String location,
        String email,
        String githubUrl,
        String linkedinUrl,
        String cvUrl,
        String cip
) {

    public static SiteProfileDto from(SiteProfile siteProfile) {
        return new SiteProfileDto(
                siteProfile.getName(),
                siteProfile.getRole(),
                siteProfile.getTagline(),
                siteProfile.getLocation(),
                siteProfile.getEmail(),
                siteProfile.getGithubUrl(),
                siteProfile.getLinkedinUrl(),
                siteProfile.getCvUrl(),
                siteProfile.getCip()
        );
    }
}
