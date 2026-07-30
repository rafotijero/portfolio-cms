package dev.rafotijero.cms.repository;

import dev.rafotijero.cms.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

    List<Skill> findAllByOrderByDisplayOrderAsc();
}
