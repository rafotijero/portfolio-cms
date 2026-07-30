# portfolio-cms

Backend headless CMS para un sitio de portfolio personal: API REST en Spring Boot que gestiona posts de blog, proyectos, certificaciones, experiencia laboral, habilidades técnicas, contenido de "sobre mí" y configuración global del sitio.

Este repo es solo el backend/API. El sitio público que consume esta API vive en un repo aparte; la interfaz de administración de contenido está pensada como otro proyecto separado, aún no creado.

## Stack

- Java 17 · Spring Boot 4.1.0
- Spring Web MVC, Spring Data JPA, Spring Security
- PostgreSQL (Neon) · Flyway para migraciones
- Maven (via wrapper)

## Requisitos

- JDK 17
- Una instancia de PostgreSQL accesible (el proyecto está pensado para Neon)

## Configuración

La app lee el datasource desde variables de entorno:

```
DB_HOST=tu-host.neon.tech
DB_NAME=tu_db
DB_USER=tu_user
DB_PASSWORD=tu_password
```

## Correr localmente

```
./mvnw spring-boot:run
```

El servidor queda en `http://localhost:8080`.

## Tests

```
./mvnw test
```

## Endpoints disponibles

Todos de solo lectura por ahora — el lado de escritura (`/api/v1/admin/**`) todavía no tiene login/JWT implementado.

- `GET /api/v1/posts` — lista paginada de posts publicados (`?page`, `?size`, default size 10), ordenados por fecha de publicación descendente.
- `GET /api/v1/posts/{slug}` — detalle de un post publicado; `404` si no existe o no está publicado.
- `GET /api/v1/projects` — lista de proyectos publicados, ordenados por `displayOrder`.
- `GET /api/v1/projects/{slug}` — detalle de un proyecto publicado; `404` si no existe o no está publicado.
- `GET /api/v1/certifications` — lista completa de certificaciones, ordenadas por `displayOrder`.
- `GET /api/v1/experience` — lista completa de experiencia laboral, ordenada por `displayOrder`.
- `GET /api/v1/skills` — habilidades técnicas agrupadas por categoría.
- `GET /api/v1/about` — párrafos de la sección "sobre mí".
- `GET /api/v1/site` — configuración global del sitio (nombre, tagline, redes, CV); `404` si aún no se cargó.

Las rutas bajo `/api/v1/admin/**` requieren autenticación (aún sin implementar; por ahora responden `401`).

## Arquitectura

El esquema de base de datos es propiedad de Flyway (`src/main/resources/db/migration/`), no de Hibernate — `ddl-auto` está en `validate`. Ver [CLAUDE.md](CLAUDE.md) para el detalle de arquitectura y convenciones del proyecto.
