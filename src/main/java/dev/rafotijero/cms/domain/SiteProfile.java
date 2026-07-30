package dev.rafotijero.cms.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "site_profile")
@Getter
@Setter
@NoArgsConstructor
public class SiteProfile {

    @Id
    private Boolean id = Boolean.TRUE;

    private String name;

    private String role;

    private String tagline;

    private String location;

    private String email;

    private String githubUrl;

    private String linkedinUrl;

    private String cvUrl;

    private String cip;
}
