# portfolio-cms

Backend headless CMS para un sitio de portfolio personal: API REST en Spring Boot que gestiona posts de blog, proyectos, certificaciones, experiencia laboral, habilidades técnicas, contenido de "sobre mí" y configuración global del sitio.

Este repo es solo el backend/API. El sitio público que consume esta API vive en un repo aparte; la interfaz de administración de contenido está pensada como otro proyecto separado, aún no creado.

Diagrama: [docs/diagrams/readme.html](docs/diagrams/readme.html)

Desplegado en Render: https://portfolio-cms-fphk.onrender.com (plan free — la primera request tras un rato de
inactividad puede tardar unos segundos en responder mientras el servicio arranca de nuevo).

## Stack

- Java 17 · Spring Boot 4.1.0
- Spring Web MVC, Spring Data JPA, Spring Security
- PostgreSQL (Neon) · Flyway para migraciones
- Maven (via wrapper)

## Requisitos

- JDK 17
- Una instancia de PostgreSQL accesible (el proyecto está pensado para Neon)

## Configuración

La app lee toda la configuración sensible desde variables de entorno:

```
# Datasource (PostgreSQL / Neon)
DB_HOST=tu-host.neon.tech
DB_NAME=tu_db
DB_USER=tu_user
DB_PASSWORD=tu_password

# JWT
JWT_SECRET=una-clave-larga-generada-con-openssl-rand-base64-48
JWT_EXPIRATION_MINUTES=720   # opcional, default 720 (12h)

# Cloudflare R2 (almacenamiento de media_assets, S3-compatible)
R2_ACCOUNT_ID=tu-account-id
R2_ACCESS_KEY_ID=tu-access-key-id
R2_SECRET_ACCESS_KEY=tu-secret-access-key
R2_BUCKET_NAME=tu-bucket
R2_PUBLIC_URL=https://tu-dominio-publico
```

Ver [docs/MEDIA_UPLOADS.md](docs/MEDIA_UPLOADS.md) para cómo crear el bucket de R2 y obtener esos valores.

## Correr localmente

```
./mvnw spring-boot:run
```

El servidor queda en `http://localhost:8080`.

## Tests

```
./mvnw test
```

No hay perfil de datasource separado para tests: corren contra la misma base de datos real configurada arriba
(con rollback transaccional automático para no dejar datos de prueba). Ver [docs/TESTING.md](docs/TESTING.md)
para la estrategia completa.

## Endpoints disponibles

### Lectura pública

- `GET /api/v1/posts` — lista paginada de posts publicados (`?page`, `?size`, default size 10), ordenados por fecha de publicación descendente.
- `GET /api/v1/posts/{slug}` — detalle de un post publicado; `404` si no existe o no está publicado.
- `GET /api/v1/projects` — lista de proyectos publicados, ordenados por `displayOrder`.
- `GET /api/v1/projects/{slug}` — detalle de un proyecto publicado; `404` si no existe o no está publicado.
- `GET /api/v1/certifications` — lista completa de certificaciones, ordenadas por `displayOrder`.
- `GET /api/v1/experience` — lista completa de experiencia laboral, ordenada por `displayOrder`.
- `GET /api/v1/skills` — habilidades técnicas agrupadas por categoría.
- `GET /api/v1/about` — párrafos de la sección "sobre mí".
- `GET /api/v1/site` — configuración global del sitio (nombre, tagline, redes, CV); `404` si aún no se cargó.

### Auth

- `POST /api/v1/auth/login` — `{username, password}` → `{token, expiresAt}`.

### Escritura (requieren `Authorization: Bearer <token>` de un usuario `ADMIN`)

CRUD (`POST`, `PUT /{id}`, `DELETE /{id}`) bajo `/api/v1/admin/` para `posts`, `projects`, `certifications`,
`experience`, `skills`, `about`, más `tags` (único recurso sin lectura pública propia). `posts` y `projects` además
tienen `GET` bajo `admin` que muestra todos los estados, incluyendo `DRAFT`. `site` es `PUT /api/v1/admin/site`
únicamente (upsert, es un singleton).

- `GET /api/v1/admin/media` — lista de archivos subidos, más recientes primero.
- `POST /api/v1/admin/media` — sube un archivo (`multipart/form-data`, campo `file`) a Cloudflare R2 y guarda su
  metadata; devuelve la URL pública para usarla en cualquier otro recurso (imagen de post/proyecto, CV, etc.).
- `DELETE /api/v1/admin/media/{id}` — borra el archivo de R2 y su fila.

## Arquitectura

El esquema de base de datos es propiedad de Flyway (`src/main/resources/db/migration/`), no de Hibernate — `ddl-auto` está en `validate`. Ver [CLAUDE.md](CLAUDE.md) para el detalle de arquitectura y convenciones del proyecto, [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) para el razonamiento detrás de las decisiones de diseño, y [docs/REQUEST_FLOW.md](docs/REQUEST_FLOW.md) para el recorrido de un request, el `Dockerfile` y el despliegue.
