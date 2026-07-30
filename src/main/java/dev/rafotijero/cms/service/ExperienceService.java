package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.ExperienceDto;
import dev.rafotijero.cms.repository.ExperienceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
