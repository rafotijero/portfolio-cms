package dev.rafotijero.cms.repository;

import dev.rafotijero.cms.domain.AboutParagraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AboutParagraphRepository extends JpaRepository<AboutParagraph, UUID> {

    List<AboutParagraph> findAllByOrderByDisplayOrderAsc();
}
