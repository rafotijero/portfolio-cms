package dev.rafotijero.cms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "experience")
@SQLDelete(sql = "UPDATE experience SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String role;

    private String company;

    private LocalDate startDate;

    private LocalDate endDate;

    private String summary;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tech_stack", columnDefinition = "text[]")
    private List<String> techStack = new ArrayList<>();

    private String logoUrl;

    private Integer displayOrder;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private Instant deletedAt;
}
