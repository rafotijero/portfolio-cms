package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.ExperienceDto;
import dev.rafotijero.cms.api.dto.ExperienceRequest;
import dev.rafotijero.cms.domain.Experience;
import dev.rafotijero.cms.repository.ExperienceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ExperienceService {

    private final ExperienceRepository experienceRepository;

    public ExperienceService(ExperienceRepository experienceRepository) {
        this.experienceRepository = experienceRepository;
    }

    public List<ExperienceDto> findAll() {
        return experienceRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(ExperienceDto::from)
                .toList();
    }

    @Transactional
    public ExperienceDto create(ExperienceRequest request) {
        Experience experience = new Experience();
        apply(experience, request);
        return ExperienceDto.from(experienceRepository.save(experience));
    }

    @Transactional
    public ExperienceDto update(UUID id, ExperienceRequest request) {
        Experience experience = getOrThrow(id);
        apply(experience, request);
        return ExperienceDto.from(experienceRepository.save(experience));
    }

    @Transactional
    public void delete(UUID id) {
        Experience experience = getOrThrow(id);
        experienceRepository.delete(experience);
    }

    private Experience getOrThrow(UUID id) {
        return experienceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void apply(Experience experience, ExperienceRequest request) {
        experience.setRole(request.role());
        experience.setCompany(request.company());
        experience.setStartDate(request.startDate());
        experience.setEndDate(request.endDate());
        experience.setSummary(request.summary());
        experience.setTechStack(request.techStack() != null ? request.techStack() : new ArrayList<>());
        experience.setLogoUrl(request.logoUrl());
        experience.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
    }
}
