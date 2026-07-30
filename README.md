# portfolio-cms

Backend CMS para un sitio de portfolio personal: API REST en Spring Boot que gestiona posts de blog, proyectos, certificaciones, tags y assets multimedia.

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

- `GET /api/v1/posts` — lista paginada de posts publicados (`?page`, `?size`, default size 10), ordenados por fecha de publicación descendente.
- `GET /api/v1/posts/{slug}` — detalle de un post publicado; `404` si no existe o no está publicado.

Las rutas bajo `/api/v1/admin/**` requieren autenticación (aún sin implementar; por ahora responden `401`).

## Arquitectura

El esquema de base de datos es propiedad de Flyway (`src/main/resources/db/migration/`), no de Hibernate — `ddl-auto` está en `validate`. Ver [CLAUDE.md](CLAUDE.md) para el detalle de arquitectura y convenciones del proyecto.
