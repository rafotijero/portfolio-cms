package dev.rafotijero.cms.api.dto;

import dev.rafotijero.cms.domain.Certification;

import java.time.LocalDate;
import java.util.UUID;

public record CertificationDto(
        UUID id,
        String name,
        String issuer,
        LocalDate issueDate,
        LocalDate issueDateEnd,
        String hours,
        String credentialUrl,
        String imageUrl,
        String institutionLogoUrl
) {

    public static CertificationDto from(Certification certification) {
        return new CertificationDto(
                certification.getId(),
                certification.getName(),
                certification.getIssuer(),
                certification.getIssueDate(),
                certification.getIssueDateEnd(),
                certification.getHours(),
                certification.getCredentialUrl(),
                certification.getImageUrl(),
                certification.getInstitutionLogoUrl()
        );
    }
}
