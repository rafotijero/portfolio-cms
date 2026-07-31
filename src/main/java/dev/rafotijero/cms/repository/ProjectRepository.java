package dev.rafotijero.cms.repository;

import dev.rafotijero.cms.domain.ContentStatus;
import dev.rafotijero.cms.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findBySlug(String slug);

    List<Project> findByStatusOrderByDisplayOrderAsc(ContentStatus status);

    List<Project> findAllByOrderByDisplayOrderAsc();
}
