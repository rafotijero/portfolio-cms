package dev.rafotijero.cms.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_assets")
@SQLDelete(sql = "UPDATE media_assets SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String filename;

    private String url;

    private String contentType;

    private Long sizeBytes;

    @CreationTimestamp
    private Instant uploadedAt;

    private Instant deletedAt;
}
