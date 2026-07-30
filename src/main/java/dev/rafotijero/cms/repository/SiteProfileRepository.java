package dev.rafotijero.cms.repository;

import dev.rafotijero.cms.domain.SiteProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteProfileRepository extends JpaRepository<SiteProfile, Boolean> {
}
