package dev.rafotijero.cms.repository;

import dev.rafotijero.cms.domain.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CertificationRepository extends JpaRepository<Certification, UUID> {

    List<Certification> findAllByOrderByDisplayOrderAsc();
}
