package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.CertificationDto;
import dev.rafotijero.cms.repository.CertificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
