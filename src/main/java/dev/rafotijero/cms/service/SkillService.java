package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.SkillGroupDto;
import dev.rafotijero.cms.api.dto.SkillItemDto;
import dev.rafotijero.cms.domain.Skill;
import dev.rafotijero.cms.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
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
}
