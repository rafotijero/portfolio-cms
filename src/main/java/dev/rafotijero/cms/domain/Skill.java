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
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "skills")
@SQLDelete(sql = "UPDATE skills SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String skillGroup;

    private String name;

    private String icon;

    private Integer displayOrder;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private Instant deletedAt;
}
