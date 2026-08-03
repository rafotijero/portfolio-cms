package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.CertificationDto;
import dev.rafotijero.cms.api.dto.CertificationRequest;
import dev.rafotijero.cms.domain.Certification;
import dev.rafotijero.cms.repository.CertificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CertificationService {

    private final CertificationRepository certificationRepository;

    public CertificationService(CertificationRepository certificationRepository) {
        this.certificationRepository = certificationRepository;
    }

    public List<CertificationDto> findAll() {
        return certificationRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(CertificationDto::from)
                .toList();
    }

    @Transactional
    public CertificationDto create(CertificationRequest request) {
        Certification certification = new Certification();
        apply(certification, request);
        return CertificationDto.from(certificationRepository.saveAndFlush(certification));
    }

    @Transactional
    public CertificationDto update(UUID id, CertificationRequest request) {
        Certification certification = getOrThrow(id);
        apply(certification, request);
        return CertificationDto.from(certificationRepository.saveAndFlush(certification));
    }

    @Transactional
    public void delete(UUID id) {
        Certification certification = getOrThrow(id);
        certificationRepository.delete(certification);
    }

    private Certification getOrThrow(UUID id) {
        return certificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void apply(Certification certification, CertificationRequest request) {
        certification.setName(request.name());
        certification.setIssuer(request.issuer());
        certification.setIssueDate(request.issueDate());
        certification.setIssueDateEnd(request.issueDateEnd());
        certification.setHours(request.hours());
        certification.setCredentialUrl(request.credentialUrl());
        certification.setImageUrl(request.imageUrl());
        certification.setInstitutionLogoUrl(request.institutionLogoUrl());
        certification.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
    }
}
