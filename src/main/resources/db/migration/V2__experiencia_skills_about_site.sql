-- V2__experiencia_skills_about_site.sql
-- Contenido del portfolio que hoy vive hardcodeado en el frontend (src/data/*.ts)
-- y pasa a ser gestionado por el CMS: experiencia laboral, habilidades tecnicas,
-- parrafos de "sobre mi" y configuracion global del sitio.

CREATE TABLE experience (
                            id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            role          VARCHAR(150) NOT NULL,
                            company       VARCHAR(150) NOT NULL,
                            start_date    DATE         NOT NULL,
                            end_date      DATE,
                            summary       TEXT         NOT NULL,
                            tech_stack    TEXT[]       NOT NULL DEFAULT '{}',
                            logo_url      VARCHAR(500),
                            display_order INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE skills (
                        id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        skill_group   VARCHAR(100) NOT NULL,
                        name          VARCHAR(100) NOT NULL,
                        icon          VARCHAR(100),
                        display_order INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE about_paragraphs (
                                  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  content       TEXT    NOT NULL,
                                  display_order INTEGER NOT NULL DEFAULT 0
);

-- Configuracion global del sitio: una sola fila. El id booleano forzado a TRUE
-- impide que exista mas de una fila (CHECK + PK sobre una columna que solo puede
-- valer TRUE).
CREATE TABLE site_profile (
                              id           BOOLEAN PRIMARY KEY DEFAULT TRUE CHECK (id),
                              name         VARCHAR(150) NOT NULL,
                              role         VARCHAR(150) NOT NULL,
                              tagline      TEXT         NOT NULL,
                              location     VARCHAR(150),
                              email        VARCHAR(150) NOT NULL,
                              github_url   VARCHAR(500),
                              linkedin_url VARCHAR(500),
                              cv_url       VARCHAR(500),
                              cip          VARCHAR(50)
);

-- La tabla certifications de V1 modelaba una fecha unica de emision y una imagen
-- de credencial; el frontend actual necesita ademas duracion en horas, un rango
-- de fechas (cursos que abarcan varios meses) y el logo de la institucion (que
-- se repite entre certificaciones de la misma institucion, es distinto de la
-- imagen de la credencial).
ALTER TABLE certifications
    ADD COLUMN hours VARCHAR(20),
    ADD COLUMN issue_date_end DATE,
    ADD COLUMN institution_logo_url VARCHAR(500);
