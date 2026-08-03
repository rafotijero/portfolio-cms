package dev.rafotijero.cms.repository;

import dev.rafotijero.cms.domain.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {

    List<MediaAsset> findAllByOrderByUploadedAtDesc();
}
