package dev.rafotijero.cms.api;

import dev.rafotijero.cms.api.dto.CertificationDto;
import dev.rafotijero.cms.api.dto.CertificationRequest;
import dev.rafotijero.cms.service.CertificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/certifications")
public class AdminCertificationController {

    private final CertificationService certificationService;

    public AdminCertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CertificationDto create(@Valid @RequestBody CertificationRequest request) {
        return certificationService.create(request);
    }

    @PutMapping("/{id}")
    public CertificationDto update(@PathVariable UUID id, @Valid @RequestBody CertificationRequest request) {
        return certificationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        certificationService.delete(id);
    }
}
