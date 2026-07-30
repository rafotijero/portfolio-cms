# CLAUDE.md

Este archivo proporciona guía a Claude Code (claude.ai/code) al trabajar con código en este repositorio.

## Proyecto

`portfolio-cms` es el backend del CMS para un sitio de portfolio personal: una API REST en Spring Boot 4.1.0 (Java 17)
respaldada por PostgreSQL (Neon), que gestiona posts de blog, proyectos, certificaciones, tags y assets multimedia.
El proyecto está en etapa temprana de scaffold — por ahora solo existen el entrypoint de la aplicación, una migración
de Flyway y el test por defecto de carga de contexto.

- Paquete base: `dev.rafotijero.cms`
- Herramienta de build: Maven (usar el wrapper, no un `mvn` del sistema)

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

La URL del datasource se construye como `jdbc:postgresql://${DB_HOST}/${DB_NAME}?sslmode=require` (apunta a Neon,
por lo que SSL es obligatorio).

## Arquitectura

- **El esquema es propiedad de Flyway, no de Hibernate.** `spring.jpa.hibernate.ddl-auto` está configurado como
  `validate` — Hibernate nunca crea ni altera tablas. Todo cambio de esquema debe hacerse mediante una nueva
  migración versionada en `src/main/resources/db/migration/` (`V2__...sql`, `V3__...sql`, ...) siguiendo la
  convención de nombres de `V1__esquema_inicial.sql`. Las entidades JPA deben mantenerse sincronizadas con lo que
  defina la última migración, o la app fallará al iniciar.
- **`open-in-view` está deshabilitado.** No hay lazy-loading fuera de los límites transaccionales/de servicio —
  el service layer debe traer todo lo que necesite el controller, sin depender de que la capa de vista dispare
  fetches lazy de JPA.
- **Modelo de dominio** (definido en `V1__esquema_inicial.sql`, todas las PKs son `UUID` vía `gen_random_uuid()`):
  - `users` — un único rol `ADMIN` (restringido por CHECK; es un CMS de un solo operador, no multi-tenant)
  - `posts` — posts de blog con estado `DRAFT`/`PUBLISHED`, ruteo por slug, etiquetados vía `post_tags`
  - `tags`, `post_tags` — relación muchos-a-muchos para etiquetar posts
  - `projects` — proyectos del portfolio con `tech_stack` como `TEXT[]` nativo de Postgres, flag `featured`,
    `display_order` manual, estado propio `DRAFT`/`PUBLISHED`
  - `certifications` — credenciales con `display_order` manual
  - `media_assets` — metadata de archivos subidos (no los archivos en sí)
  - Las columnas de estado/orden (`status`, `display_order`) existen específicamente para soportar los endpoints
    públicos de listado/detalle — los índices `idx_posts_status_published_at` e `idx_projects_status_order` están
    optimizados para esas consultas.
- El starter de Security está incluido pero aún no existe configuración de `SecurityFilterChain`/auth — se espera
  definirla al agregar endpoints de escritura para el admin (la tabla `users`/rol `ADMIN` sugiere auth de un solo
  administrador, probablemente vía JWT o sesión, no un sistema de auth multi-usuario completo).
