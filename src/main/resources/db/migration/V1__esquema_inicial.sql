-- V1__esquema_inicial.sql
-- Esquema inicial del CMS del portfolio (PostgreSQL / Neon)

CREATE TABLE users (
                       id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       username      VARCHAR(50)  NOT NULL UNIQUE,
                       password_hash VARCHAR(100) NOT NULL,
                       role          VARCHAR(20)  NOT NULL DEFAULT 'ADMIN'
                           CHECK (role IN ('ADMIN'))
);

CREATE TABLE tags (
                      id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      name VARCHAR(50) NOT NULL UNIQUE,
                      slug VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE posts (
                       id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       title           VARCHAR(200) NOT NULL,
                       slug            VARCHAR(220) NOT NULL UNIQUE,
                       summary         VARCHAR(500),
                       content         TEXT         NOT NULL,
                       cover_image_url VARCHAR(500),
                       status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
                           CHECK (status IN ('DRAFT', 'PUBLISHED')),
                       published_at    TIMESTAMPTZ,
                       created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
                       updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE post_tags (
                           post_id UUID NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
                           tag_id  UUID NOT NULL REFERENCES tags (id)  ON DELETE CASCADE,
                           PRIMARY KEY (post_id, tag_id)
);

CREATE TABLE projects (
                          id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          name            VARCHAR(150) NOT NULL,
                          slug            VARCHAR(170) NOT NULL UNIQUE,
                          description     TEXT,
                          tech_stack      TEXT[]       NOT NULL DEFAULT '{}',
                          repo_url        VARCHAR(500),
                          live_url        VARCHAR(500),
                          cover_image_url VARCHAR(500),
                          featured        BOOLEAN      NOT NULL DEFAULT FALSE,
                          display_order   INTEGER      NOT NULL DEFAULT 0,
                          status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
                              CHECK (status IN ('DRAFT', 'PUBLISHED'))
);

CREATE TABLE certifications (
                                id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                name           VARCHAR(200) NOT NULL,
                                issuer         VARCHAR(150) NOT NULL,
                                issue_date     DATE         NOT NULL,
                                credential_url VARCHAR(500),
                                image_url      VARCHAR(500),
                                display_order  INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE media_assets (
                              id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              filename     VARCHAR(255) NOT NULL,
                              url          VARCHAR(500) NOT NULL,
                              content_type VARCHAR(100) NOT NULL,
                              size_bytes   BIGINT       NOT NULL,
                              uploaded_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Índices para las consultas públicas más frecuentes
CREATE INDEX idx_posts_status_published_at ON posts (status, published_at DESC);
CREATE INDEX idx_projects_status_order     ON projects (status, display_order);