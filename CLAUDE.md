# CLAUDE.md

Este archivo proporciona guía a Claude Code (claude.ai/code) al trabajar con código en este repositorio.

## Proyecto

`portfolio-cms` es el backend headless CMS para un sitio de portfolio personal: una API REST en Spring Boot 4.1.0
(Java 17) respaldada por PostgreSQL (Neon). Gestiona posts de blog, proyectos, certificaciones, experiencia laboral,
habilidades técnicas, contenido de "sobre mí" y configuración global del sitio.

Este repo es solo el backend/API. El sitio público que consume esta API vive en un repo aparte (`portfolio`, Astro);
la interfaz de administración de contenido (donde se escribe el contenido) está pensada como otro proyecto separado
(`portfolio-cms-admin`), aún no creado.

- Paquete base: `dev.rafotijero.cms`
- Herramienta de build: Maven (usar el wrapper, no un `mvn` del sistema)

## Deployment

Desplegado en Render (plan free, Docker) desde la rama `main` — cada push a `main` redespliega automáticamente.
`Dockerfile` en la raíz hace build multi-stage con `mvnw` (requiere que `mvnw` mantenga el bit de ejecución en git;
si `git ls-files -s mvnw` no muestra `100755`, el build de Docker falla con "Permission denied" — corregir con
`git update-index --chmod=+x mvnw`). `server.port` lee `${PORT:8080}` porque Render asigna el puerto en runtime, no
siempre 8080. Variables de entorno configuradas en el dashboard de Render (las mismas que en `.env.local`: `DB_HOST`,
`DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`, `R2_ACCOUNT_ID`, `R2_ACCESS_KEY_ID`,
`R2_SECRET_ACCESS_KEY`, `R2_BUCKET_NAME`, `R2_PUBLIC_URL`, `CORS_ALLOWED_ORIGINS`).

## Comandos

Usar el Maven wrapper (`mvnw.cmd` en Windows, `./mvnw` en Git Bash/WSL):

```
./mvnw clean install       # build completo
./mvnw compile              # solo compilar
./mvnw spring-boot:run       # correr la app localmente
./mvnw test                  # correr todos los tests
./mvnw test -Dtest=PortfolioCmsApplicationTests#contextLoads   # correr un método de test específico
./mvnw test -Dtest=PortfolioCmsApplicationTests                # correr una clase de test específica
```

Nota: no existe un perfil de datasource específico para tests. `contextLoads` (y cualquier `@SpringBootTest`) levanta
el contexto completo de Spring, lo cual requiere una instancia de PostgreSQL alcanzable y las variables de entorno
del datasource (ver abajo) — los tests fallarán sin conectividad a la base de datos.

## Configuración

`src/main/resources/application.yaml` lee el datasource enteramente desde variables de entorno — deben configurarse
antes de correr la app o los tests:

- `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `JWT_SECRET` — clave HMAC para firmar los JWT (sin default a propósito; la app no arranca sin ella). Generar con
  algo como `openssl rand -base64 48`, nunca hardcodear un valor por defecto en el código.
- `JWT_EXPIRATION_MINUTES` — opcional, default `720` (12h).
- `R2_ACCOUNT_ID`, `R2_ACCESS_KEY_ID`, `R2_SECRET_ACCESS_KEY`, `R2_BUCKET_NAME`, `R2_PUBLIC_URL` — credenciales y
  bucket de Cloudflare R2 para `media_assets`. Ver [docs/MEDIA_UPLOADS.md](docs/MEDIA_UPLOADS.md) para cómo
  crearlas.
- `CORS_ALLOWED_ORIGINS` — opcional, default `http://localhost:5173` (dev de `portfolio-cms-admin`). Lista
  separada por comas de orígenes permitidos para llamar a la API desde un browser (`config/SecurityConfig.java`).

La URL del datasource se construye como `jdbc:postgresql://${DB_HOST}/${DB_NAME}?sslmode=require` (apunta a Neon,
por lo que SSL es obligatorio). Para desarrollo local, poner estas variables en un `.env.local` (gitignored) y
cargarlas en el shell antes de correr `./mvnw spring-boot:run`; no pedir ni pegar credenciales en texto plano.

No hay endpoint de registro (el CMS es de un solo admin). El usuario se siembra directo en la tabla `users` con un
script puntual que hashea con `BCryptPasswordEncoder` — ver el patrón usado la primera vez antes de reinventar otro.

## Arquitectura

- **El esquema es propiedad de Flyway, no de Hibernate.** `spring.jpa.hibernate.ddl-auto` está configurado como
  `validate` — Hibernate nunca crea ni altera tablas. Todo cambio de esquema debe hacerse mediante una nueva
  migración versionada en `src/main/resources/db/migration/` (`V3__...sql`, ...) siguiendo la convención de nombres
  existente. Las entidades JPA deben mantenerse sincronizadas con lo que defina la última migración, o la app
  fallará al iniciar. Nunca editar una migración ya aplicada (`V1__esquema_inicial.sql`,
  `V2__experiencia_skills_about_site.sql`).
- **`open-in-view` está deshabilitado.** No hay lazy-loading fuera de los límites transaccionales/de servicio — los
  métodos de servicio que devuelven DTOs con relaciones lazy (p. ej. `Post.tags`) deben ser `@Transactional`, o el
  mapeo a DTO lanza `LazyInitializationException` fuera de la sesión de Hibernate.
- **Paquetes**: `domain` (entidades JPA), `repository` (interfaces `JpaRepository`), `service` (lógica de negocio,
  transaccional), `api` + `api.dto` (controllers REST y DTOs como `record`), `config` (beans de configuración como
  `SecurityConfig`), `security` (`JwtService`, `JwtAuthenticationFilter` — piezas específicas de JWT, separadas de
  `config` para no mezclar la definición de la cadena de filtros con la lógica de tokens). Las entidades nunca se
  exponen directamente en los controllers.
- **Seguridad** (`config/SecurityConfig`): `GET /api/v1/**` es público salvo `/api/v1/admin/**` (requiere rol
  `ADMIN` vía JWT) y `/api/v1/auth/**` (denegado salvo `POST /api/v1/auth/login`, el único endpoint público ahí).
  Sesión `STATELESS`, CSRF deshabilitado. `/error` está explícitamente permitido: sin eso, el forward interno que
  hace Spring para renderizar errores (404, 500) vuelve a pasar por el filtro de seguridad y queda bloqueado por la
  regla de denegación general, enmascarando cualquier error real como `401`. CORS habilitado vía
  `CorsConfigurationSource` (origins desde `cors.allowed-origins`, ver Configuración) para que `portfolio-cms-admin`
  (otro origen del browser) pueda llamar a la API; `OPTIONS /**` está permitido explícitamente porque si no, el
  preflight cae en la regla de denegación general igual que pasaba con `/error`.
- **JWT**: `JwtService` firma/valida con HMAC (`jwt.secret`, sin default — ver Configuración), subject = `username`,
  claim `role`. `JwtAuthenticationFilter` (`OncePerRequestFilter`, registrado con `addFilterBefore` antes de
  `UsernamePasswordAuthenticationFilter`) lee el header `Authorization: Bearer <token>`, valida y puebla el
  `SecurityContext` con autoridad `ROLE_<role>`. Sin refresh tokens ni revocación — deliberadamente simple para un
  CMS de un solo admin; la mitigación es una expiración corta (`jwt.expiration-minutes`, default 720 = 12h).
  `UserDetailsServiceAutoConfiguration` está excluida en `application.yaml` (paquete
  `org.springframework.boot.security.autoconfigure` en Spring Boot 4.1, distinto del paquete de versiones
  anteriores) porque el login no pasa por `UserDetailsService`/`AuthenticationManager` — `AuthService` valida
  directo contra `UserRepository` + `PasswordEncoder`, sin esa autoconfiguración quedaba un usuario en memoria con
  password generado que no se usa para nada.
- **Modelo de dominio** (todas las PKs son `UUID` vía `gen_random_uuid()` salvo donde se indica):
  - `users` — un único rol `ADMIN` (restringido por CHECK; es un CMS de un solo operador, no multi-tenant)
  - `posts` — posts de blog con estado `DRAFT`/`PUBLISHED`, ruteo por slug, etiquetados vía `post_tags`
  - `tags`, `post_tags` — relación muchos-a-muchos para etiquetar posts
  - `projects` — proyectos del portfolio con `tech_stack` como `TEXT[]` nativo de Postgres, flag `featured`,
    `display_order` manual, estado propio `DRAFT`/`PUBLISHED`
  - `certifications` — credenciales, con `hours`, rango `issue_date`/`issue_date_end` y `institution_logo_url`
    (logo de la institución, distinto de `image_url` que es la imagen de la credencial en sí) además de
    `display_order` manual
  - `experience` — historial laboral con `start_date`/`end_date` (nullable = puesto actual), `tech_stack` como
    `TEXT[]`, `display_order` manual
  - `skills` — habilidades técnicas agrupadas por `skill_group` (texto libre, sin tabla de grupos separada — YAGNI
    mientras el número de grupos sea pequeño), `display_order` manual
  - `about_paragraphs` — párrafos de la sección "sobre mí", ordenados por `display_order`
  - `site_profile` — configuración global del sitio (nombre, tagline, redes, CV, etc.); **fila única forzada**: PK
    `BOOLEAN` con `DEFAULT TRUE CHECK (id)`, así la tabla nunca puede tener más de una fila
  - `media_assets` — metadata de archivos subidos (no los archivos en sí)
  - Las columnas de estado/orden (`status`, `display_order`) en `posts`/`projects` existen específicamente para
    soportar los endpoints públicos de listado/detalle — los índices `idx_posts_status_published_at` e
    `idx_projects_status_order` están optimizados para esas consultas. `experience`, `skills`, `about_paragraphs` y
    `site_profile` no tienen estado `DRAFT`/`PUBLISHED`: son contenido simple sin flujo editorial, a diferencia de
    posts/projects.
- **Auditoría y borrado lógico** (`V3__auditoria_y_borrado_logico.sql`): `posts`, `projects`, `certifications`,
  `experience`, `skills`, `about_paragraphs`, `tags` y `media_assets` tienen `created_at`/`updated_at` (menos
  `media_assets`, que solo tiene `uploaded_at` — no hay endpoint de update) y `deleted_at` (nullable, `NULL` = no
  borrado). `users` y `site_profile` quedan afuera a propósito: ninguno tiene endpoint `DELETE`. El mecanismo es
  vía hooks de Hibernate en la entidad, no filtrado manual: `@SQLDelete` reemplaza el `DELETE FROM` real por un
  `UPDATE ... SET deleted_at = now()`, y `@SQLRestriction("deleted_at IS NULL")` hace que toda consulta JPA
  (`findAll`, `findById`, `findBySlug`, etc.) excluya automáticamente las filas borradas — ningún
  service/repository/controller necesita saber que el borrado es lógico, `repository.delete(entity)` ya hace lo
  correcto. `deletedAt` nunca se expone en ningún DTO (siempre sería `null` en cualquier respuesta real, gracias a
  `@SQLRestriction`) ni se setea desde código de aplicación. En `media_assets` esto es asimétrico a propósito: el
  `DELETE` sigue borrando el archivo real de R2 (`s3Client.deleteObject`, código de aplicación ajeno al hook), solo
  la fila de metadata queda como soft-delete — no hay forma de "recuperar" el archivo, solo el rastro de auditoría.
  **Gotcha de flush ya conocido** (mismo patrón que el bug de `MediaAsset.uploadedAt`, ver
  `docs/TESTING.md`): todos los `create()`/`update()` de los services afectados usan `saveAndFlush`, no `save` —
  con `save` a secas, `@CreationTimestamp`/`@UpdateTimestamp` quedan `null` en la respuesta porque Hibernate no
  manda el INSERT/UPDATE real hasta un flush.
- **Conflictos de unicidad**: `GlobalExceptionHandler` (`api/GlobalExceptionHandler.java`) mapea
  `DataIntegrityViolationException` → `409 Conflict` en vez del `500` que tirarían por defecto los índices únicos de
  slugs (`posts`, `projects`) y de `tags` (`name`, `slug`). Desde `V3`, esos ya no son `UNIQUE` de columna simple
  sino **índices únicos parciales** (`CREATE UNIQUE INDEX ... WHERE deleted_at IS NULL`): un `UNIQUE` inline
  bloquearía reusar un `name`/`slug` después de un borrado lógico, porque la fila "borrada" sigue existiendo
  físicamente con ese valor. Cualquier migración futura que agregue una columna `UNIQUE` a una tabla con
  `deleted_at` debe usar este mismo patrón de índice parcial, no un `UNIQUE` inline. Aplica a cualquier controller,
  no hay que repetir el manejo.
- **Endpoints de lectura pública** (sin token):
  - `GET /api/v1/posts` — paginado (`?page`, `?size`, default 10), solo `PUBLISHED`, orden `publishedAt` desc
  - `GET /api/v1/posts/{slug}` — detalle; `404` si no existe o no está `PUBLISHED`
  - `GET /api/v1/experience` — lista completa ordenada por `displayOrder`, sin paginar (contenido acotado)
  - `GET /api/v1/skills` — lista agrupada por `skillGroup` (`[{group, items: [{name, icon}]}]`); el agrupamiento se
    hace en `SkillService` preservando el orden de `displayOrder` (`Collectors.groupingBy` con `LinkedHashMap`)
  - `GET /api/v1/about` — lista de strings (los párrafos), sin DTO propio por ser un solo campo relevante
  - `GET /api/v1/site` — `SiteProfileDto`; `404` si `site_profile` no tiene fila cargada todavía
  - `GET /api/v1/projects` — solo `PUBLISHED`, ordenado por `displayOrder`, sin paginar (contenido acotado, a
    diferencia de posts)
  - `GET /api/v1/projects/{slug}` — detalle; `404` si no existe o no está `PUBLISHED`
  - `GET /api/v1/certifications` — lista completa ordenada por `displayOrder` (sin estado editorial, como
    experience/skills/about)
- **Auth**:
  - `POST /api/v1/auth/login` — `{username, password}` → `{token, expiresAt}`; `401` genérico si el usuario no
    existe o la password no calza (mismo mensaje en ambos casos, para no filtrar qué username existe)
- **Endpoints de escritura** (todos bajo `/api/v1/admin/**`, exigen JWT con rol `ADMIN`):
  - `tags` — único recurso sin lectura pública propia (los posts solo exponen nombres de tag, no ids): CRUD completo
    (`GET` lista, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`) en `AdminTagController`, necesario para poder
    referenciar tags por id al crear/editar posts.
  - `posts`, `projects` — además del CRUD (`POST`, `PUT /{id}`, `DELETE /{id}`), tienen `GET` (lista paginada /
    lista simple) y `GET /{id}` bajo `admin` que devuelven **todos los estados** (`DRAFT` incluido), a diferencia
    del endpoint público que solo muestra `PUBLISHED` — así el admin puede ver y editar borradores.
    `PostRequest.tagIds` (`Set<UUID>`) resuelve contra `TagRepository`; si algún id no existe, `400`.
    `PostSummaryDto`/`PostDetailDto`/`ProjectDto` incluyen `status` (y `ProjectDto` también `displayOrder`) — son
    los mismos DTOs para lectura pública y admin, agregar el campo no expone nada sensible porque el público solo
    ve `PUBLISHED` de todas formas.
  - `certifications`, `experience`, `skills`, `about` — solo `POST`/`PUT /{id}`/`DELETE /{id}`, sin `GET` bajo
    `admin`: como no tienen estado editorial, el endpoint público ya lista todo lo que existe, no hace falta
    duplicarlo. `skills` y `about` sí necesitaron DTOs planos nuevos (`SkillDto`, `AboutParagraphDto`) porque las
    respuestas públicas usan formas agregadas (`SkillGroupDto`, `List<String>`) que no sirven para editar una fila
    individual.
  - `site` — `PUT /api/v1/admin/site` únicamente (upsert): no hay `POST` ni `DELETE` porque `site_profile` es
    singleton por esquema; `SiteProfileService.save` hace `findById(TRUE).orElseGet(new)` y guarda.
  - `media` — `GET`/`POST` (`multipart/form-data`, campo `file`)/`DELETE /{id}` bajo `/api/v1/admin/media`, sin
    lectura pública propia (igual que `tags`): el admin sube el archivo a Cloudflare R2 y copia la URL pública que
    devuelve a mano en el campo de texto plano que corresponda (`Post.coverImageUrl`, `SiteProfile.cvUrl`, etc. —
    ninguno tiene FK a `media_assets`). Ver [docs/MEDIA_UPLOADS.md](docs/MEDIA_UPLOADS.md) para el detalle de
    implementación y cómo crear el bucket.
