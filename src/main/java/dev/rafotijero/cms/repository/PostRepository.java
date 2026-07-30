package dev.rafotijero.cms.repository;

import dev.rafotijero.cms.domain.ContentStatus;
import dev.rafotijero.cms.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findBySlug(String slug);

    Page<Post> findByStatus(ContentStatus status, Pageable pageable);
}
