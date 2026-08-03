package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.SkillDto;
import dev.rafotijero.cms.api.dto.SkillGroupDto;
import dev.rafotijero.cms.api.dto.SkillItemDto;
import dev.rafotijero.cms.api.dto.SkillRequest;
import dev.rafotijero.cms.domain.Skill;
import dev.rafotijero.cms.repository.SkillRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<SkillGroupDto> findAllGrouped() {
        List<Skill> skills = skillRepository.findAllByOrderByDisplayOrderAsc();

        LinkedHashMap<String, List<SkillItemDto>> byGroup = skills.stream()
                .collect(Collectors.groupingBy(
                        Skill::getSkillGroup,
                        LinkedHashMap::new,
                        Collectors.mapping(s -> new SkillItemDto(s.getName(), s.getIcon()), Collectors.toList())
                ));

        return byGroup.entrySet().stream()
                .map(entry -> new SkillGroupDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Transactional
    public SkillDto create(SkillRequest request) {
        Skill skill = new Skill();
        apply(skill, request);
        return SkillDto.from(skillRepository.saveAndFlush(skill));
    }

    @Transactional
    public SkillDto update(UUID id, SkillRequest request) {
        Skill skill = getOrThrow(id);
        apply(skill, request);
        return SkillDto.from(skillRepository.saveAndFlush(skill));
    }

    @Transactional
    public void delete(UUID id) {
        Skill skill = getOrThrow(id);
        skillRepository.delete(skill);
    }

    private Skill getOrThrow(UUID id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void apply(Skill skill, SkillRequest request) {
        skill.setSkillGroup(request.skillGroup());
        skill.setName(request.name());
        skill.setIcon(request.icon());
        skill.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
    }
}
