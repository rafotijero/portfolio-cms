# Arquitectura y decisiones de diseño

Este documento explica el *por qué* detrás de las decisiones estructurales de `portfolio-cms`. Para comandos y
referencia rápida de "qué existe y dónde", ver [`CLAUDE.md`](../CLAUDE.md); para instrucciones de setup, el
[`README.md`](../README.md).

## Estilo: Controller-Service-Repository, no MVC

Es fácil llamar a esto "MVC" porque el framework literalmente se llama Spring MVC, pero no lo es en sentido
estricto — MVC clásico tiene tres piezas (Model, View, Controller) y acá falta una:

- **Controller** — existe: paquete `api` (`PostController`, `AdminPostController`, etc.).
- **View** — no existe. MVC clásico (JSP, Thymeleaf, Razor) tiene una capa que renderiza HTML a partir del modelo.
  Acá no hay HTML: el controller devuelve un `record` DTO y Jackson lo serializa a JSON automáticamente. Ese
  serializado es infraestructura del framework, no una capa de arquitectura propia.
- **Model** — en MVC clásico es una sola cosa. Acá está deliberadamente partido en tres responsabilidades:
  `domain` (la entidad, cómo se persiste), `service` (la lógica de negocio y el límite transaccional), `api.dto`
  (la forma que ve el cliente por HTTP). Esa separación es justamente lo que se conoce como **Controller-Service-
  Repository** (o arquitectura en capas / N-tier), no MVC.

Si en algún momento `portfolio-cms-admin` se construye con renderizado server-side (Thymeleaf u otro), ahí sí
aplicarían las tres piezas completas. Una SPA consumiendo esta API tampoco es MVC del lado del backend — es un
frontend con su propio patrón, hablando con un backend en capas.

## Arquitectura en capas, estricta

Cada request pasa por `domain` (entidad JPA) → `repository` (Spring Data) → `service` (lógica + límite
transaccional) → `api` / `api.dto` (controller + DTO). Las entidades **nunca** se devuelven directo al cliente,
siempre se mapean a un `record`. Esto desacopla el contrato de la API de los detalles de la base de datos: se puede
cambiar una columna sin romper el JSON que ya consume alguien.

## Separación lectura pública / escritura admin

No es un CRUD genérico — son dos superficies con reglas distintas: pública (solo contenido `PUBLISHED`, sin auth) y
admin (todos los estados, requiere JWT + rol). Se reutiliza el mismo DTO cuando tiene sentido (`Post`, `Project`,
agregando el campo `status`), y se crea un DTO nuevo cuando las formas son incompatibles (`skills`/`about`, donde
la vista pública viene agrupada/aplanada a texto y la de edición necesita filas individuales con su `id`).

## La base de datos es la fuente de verdad del esquema

Flyway gobierna el esquema; Hibernate solo valida (`ddl-auto: validate`), nunca genera ni altera tablas. Las
migraciones ya aplicadas nunca se editan — todo cambio de esquema es una migración nueva y versionada. Esto evita
el problema clásico de "en mi máquina la tabla tiene una columna que en producción no existe".

## JWT deliberadamente simple

Sin OAuth2 Resource Server — eso resuelve el caso de un Identity Provider externo, y acá el mismo servicio emite y
valida sus propios tokens. Sin refresh tokens ni revocación; la mitigación es una expiración corta
(`jwt.expiration-minutes`). El tamaño justo para un CMS de un solo admin, no lo que usaría una app enterprise
multi-tenant.

## Seguridad por lista blanca, no lista negra

El `SecurityFilterChain` deniega todo por defecto (`anyRequest().denyAll()`) y abre explícitamente solo lo que debe
ser público. Se usa rol real (`hasRole("ADMIN")`) en vez de solo "está autenticado". El login devuelve el mismo
mensaje genérico tanto si el usuario no existe como si la password está mal, para no filtrar qué usernames existen.

## Manejo de errores centralizado

Un único `GlobalExceptionHandler` traduce violaciones de unicidad de la base de datos a `409 Conflict` para todos
los controllers a la vez, en vez de repetir ese `try/catch` en cada uno. Los códigos HTTP se usan con intención:
`201` al crear, `204` al borrar, `400` en validación, `401`/`404`/`409` donde corresponde — no todo es `200` o
`500`.

## Sin secretos con valor por defecto

`JWT_SECRET` no tiene default en el código — si falta, la aplicación no arranca. Es intencional: un valor "de
emergencia" hardcodeado es exactamente el tipo de cosa que alguien olvida cambiar en producción y se convierte en
una vulnerabilidad real.

## Verificado contra la base de datos real, no solo "compila"

Cada endpoint se probó con `curl` contra la base de datos real (Neon) antes de cada commit. Esa disciplina detectó
dos bugs que "compila y pasa el build" no habría atrapado: un `LazyInitializationException` en `Post.tags` por
mapear a DTO fuera de una transacción, y un `404` real enmascarado como `401` porque el forward interno de Spring
a `/error` volvía a pasar por el filtro de seguridad.

## Configuración por entorno, deploy por git push

Todo lo sensible (credenciales de base de datos, JWT, y las de almacenamiento de archivos) vive en variables de
entorno, nunca en el repositorio. El `Dockerfile` es multi-stage para que la imagen final no cargue el JDK completo,
solo el JRE necesario para correr. El deploy se dispara con `git push` a `main` (Render redespliega automático), no
con subida manual de artefactos.
