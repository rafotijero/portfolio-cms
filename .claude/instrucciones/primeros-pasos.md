# Tarea: Backend CMS del portfolio — entidades, repositorios, security y primer endpoint

## Contexto

Proyecto Spring Boot existente y funcionando: arranca, se conecta a PostgreSQL (Neon) y Flyway ya aplicó la migración `V1__esquema_inicial.sql`. Las tablas existen en la base. **No crees el proyecto ni toques la migración V1** — solo agrega el código de aplicación descrito abajo.

- Spring Boot 4.1.0 · Java 17 · Maven
- Group: `dev.rafotijero` · Artifact: `portfolio-cms`
- Paquete raíz: `dev.rafotijero.cms` (verificar el real en el proyecto y respetarlo)
- Dependencias ya presentes: Web, Data JPA, PostgreSQL Driver, Flyway, Security, Validation, Lombok
- `application.yml` ya configurado con `ddl-auto: validate`, `open-in-view: false` y datasource por variables de entorno. **No modificarlo** salvo lo indicado en la Tarea 3.

## Esquema existente (referencia — NO regenerar)

Tablas creadas por V1: `users`, `tags`, `posts`, `post_tags`, `projects`, `certifications`, `media_assets`.

Detalles que condicionan el mapeo JPA:

- PKs: `UUID` con default `gen_random_uuid()` en BD.
- `posts.status` y `projects.status`: `VARCHAR` con CHECK `('DRAFT','PUBLISHED')`.
- `users.role`: `VARCHAR` con CHECK `('ADMIN')`.
- `posts` ↔ `tags`: many-to-many vía `post_tags (post_id, tag_id)`.
- `projects.tech_stack`: `TEXT[]` de PostgreSQL.
- Timestamps: `TIMESTAMPTZ` (`posts.created_at`, `posts.updated_at`, `posts.published_at`, `media_assets.uploaded_at`); `certifications.issue_date` es `DATE`.
- Columnas en snake_case; las entidades usan camelCase con la estrategia de naming por defecto de Spring Boot (no declarar `@Column(name=...)` salvo necesidad).

## Tarea 1 — Entidades JPA

Paquete `...cms.domain` (o el equivalente en el proyecto).

Crear exactamente estas clases, espejo de las tablas, sin campos adicionales:

1. `User` — id, username, passwordHash, role (enum `Role { ADMIN }`)
2. `Tag` — id, name, slug
3. `Post` — id, title, slug, summary, content, coverImageUrl, status (enum `ContentStatus { DRAFT, PUBLISHED }`), publishedAt, createdAt, updatedAt, tags (`Set<Tag>`, `@ManyToMany` con `@JoinTable(name = "post_tags")`)
4. `Project` — id, name, slug, description, techStack (`List<String>` mapeado al `TEXT[]`; con Hibernate 6+ el mapeo de array nativo es directo), repoUrl, liveUrl, coverImageUrl, featured, displayOrder, status (reusar `ContentStatus`)
5. `Certification` — id, name, issuer, issueDate, credentialUrl, imageUrl, displayOrder
6. `MediaAsset` — id, filename, url, contentType, sizeBytes, uploadedAt

Convenciones:

- IDs: `@Id @GeneratedValue(strategy = GenerationType.UUID)`.
- Enums: `@Enumerated(EnumType.STRING)`.
- `createdAt`/`updatedAt` de Post: `@CreationTimestamp` / `@UpdateTimestamp` de Hibernate.
- Lombok permitido: `@Getter @Setter @NoArgsConstructor`. No usar `@Data` ni `@EqualsAndHashCode` sobre entidades.

**Criterio de aceptación:** la app arranca sin errores de `validate` (Hibernate confirma que entidades y tablas calzan).

## Tarea 2 — Repositorios

Paquete `...cms.repository`. Una interfaz `extends JpaRepository<Entidad, UUID>` por entidad. Métodos derivados mínimos:

- `PostRepository`: `Optional<Post> findBySlug(String slug)`, `Page<Post> findByStatus(ContentStatus status, Pageable pageable)`
- `ProjectRepository`: `Optional<Project> findBySlug(String slug)`, `List<Project> findByStatusOrderByDisplayOrderAsc(ContentStatus status)`
- `CertificationRepository`: `List<Certification> findAllByOrderByDisplayOrderAsc()`
- `TagRepository`: `Optional<Tag> findBySlug(String slug)`
- `UserRepository`: `Optional<User> findByUsername(String username)`

Ningún método adicional.

## Tarea 3 — Configuración de Security

Paquete `...cms.config`, clase `SecurityConfig` con un bean `SecurityFilterChain`:

- `GET /api/v1/**` público (`permitAll`), **excepto** `/api/v1/admin/**` y `/api/v1/auth/**`.
- `/api/v1/admin/**`: `authenticated()` (el JWT llega en una tarea posterior; por ahora basta que responda 401).
- CSRF deshabilitado y sesión `STATELESS` (API sin estado).
- Todo lo demás: denegado.

No implementar login, JWT, ni `UserDetailsService` todavía.

## Tarea 4 — Primer endpoint público

Paquetes `...cms.api` (controller + DTOs) y `...cms.service`.

- `GET /api/v1/posts` — solo posts `PUBLISHED`, paginado (`?page`, `?size`, default size 10), ordenado por `publishedAt` desc. Respuesta: `Page<PostSummaryDto>`.
- `GET /api/v1/posts/{slug}` — detalle de un post `PUBLISHED`; 404 si no existe o no está publicado. Respuesta: `PostDetailDto`.

DTOs como `record`:

- `PostSummaryDto(UUID id, String title, String slug, String summary, String coverImageUrl, Instant publishedAt, List<String> tags)` — tags como nombres.
- `PostDetailDto` — lo mismo + `content`.

Nunca exponer entidades directamente en el controller. Lógica en un `PostService`; el controller solo delega.

**Criterio de aceptación:** con un post insertado a mano en estado `PUBLISHED`, `curl localhost:8080/api/v1/posts` devuelve el JSON paginado sin autenticación; `curl localhost:8080/api/v1/admin/posts` devuelve 401.

## Restricciones globales

- No agregar dependencias al `pom.xml`.
- No crear migraciones Flyway nuevas ni editar la V1.
- No generar tests en esta tarea (vendrán después).
- No agregar endpoints, campos ni funcionalidades no listadas aquí.