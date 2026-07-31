package dev.rafotijero.cms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CertificationRequest(
        @NotBlank String name,
        @NotBlank String issuer,
        @NotNull LocalDate issueDate,
        LocalDate issueDateEnd,
        String hours,
        String credentialUrl,
        String imageUrl,
        String institutionLogoUrl,
        Integer displayOrder
) {
}
