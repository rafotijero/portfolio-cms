-- V3__auditoria_y_borrado_logico.sql
-- Auditoria (created_at/updated_at) y borrado logico (deleted_at) para los recursos
-- con endpoint DELETE. users y site_profile quedan afuera: ninguno tiene borrado.

ALTER TABLE tags
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN deleted_at TIMESTAMPTZ;

ALTER TABLE projects
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN deleted_at TIMESTAMPTZ;

ALTER TABLE certifications
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN deleted_at TIMESTAMPTZ;

ALTER TABLE experience
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN deleted_at TIMESTAMPTZ;

ALTER TABLE skills
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN deleted_at TIMESTAMPTZ;

ALTER TABLE about_paragraphs
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN deleted_at TIMESTAMPTZ;

-- posts ya tiene created_at/updated_at (V1); solo falta deleted_at.
ALTER TABLE posts
    ADD COLUMN deleted_at TIMESTAMPTZ;

-- media_assets ya tiene uploaded_at (cumple el rol de created_at); no hay endpoint
-- de update, no aplica updated_at.
ALTER TABLE media_assets
    ADD COLUMN deleted_at TIMESTAMPTZ;

-- Los UNIQUE inline bloquearian reusar un name/slug despues de un borrado logico,
-- porque la fila "borrada" sigue existiendo fisicamente con ese valor. Se
-- reemplazan por indices unicos parciales: permiten cualquier cantidad de filas
-- borradas con ese valor, pero como mucho una fila activa (deleted_at IS NULL).
ALTER TABLE tags DROP CONSTRAINT tags_name_key;
ALTER TABLE tags DROP CONSTRAINT tags_slug_key;
CREATE UNIQUE INDEX idx_tags_name_active ON tags (name) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX idx_tags_slug_active ON tags (slug) WHERE deleted_at IS NULL;

ALTER TABLE posts DROP CONSTRAINT posts_slug_key;
CREATE UNIQUE INDEX idx_posts_slug_active ON posts (slug) WHERE deleted_at IS NULL;

ALTER TABLE projects DROP CONSTRAINT projects_slug_key;
CREATE UNIQUE INDEX idx_projects_slug_active ON projects (slug) WHERE deleted_at IS NULL;
