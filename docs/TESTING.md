# Estrategia de testing

Diagrama: [diagrams/testing.html](diagrams/testing.html)

## Cómo correr los tests

```
./mvnw test
```

No existe un perfil de datasource separado para tests (ver [CLAUDE.md](../CLAUDE.md)) — `@SpringBootTest` levanta
el contexto completo de Spring contra la **misma base de datos real** (Neon) que usa la app en desarrollo/producción,
usando las mismas variables de entorno (`DB_*`, `JWT_*`, `R2_*`, más `ADMIN_USERNAME`/`ADMIN_PASSWORD` para el test
de login real). Sin esas variables cargadas en el shell, los tests fallan al levantar el contexto, no solo el que
usa esa variable puntual.

## Aislamiento: rollback transaccional, no una base de datos aparte

Cada test class usa `@SpringBootTest @AutoConfigureMockMvc @Transactional`. Spring envuelve cada método de test en
una transacción que se revierte automáticamente al terminar, así que crear/actualizar/borrar datos de prueba
contra la base real no dejan rastro — no hace falta una base de datos de test separada ni limpiar manualmente
después de cada corrida.

Esto funciona porque MockMvc ejecuta todo en el mismo hilo (sin `webEnvironment = RANDOM_PORT`, que levantaría un
Tomcat real en otro hilo y rompería el truco): los métodos `@Transactional` de los services (con propagación
`REQUIRED` por defecto) se unen a la transacción del test en vez de abrir una propia.

**Excepción:** para probar que un conflicto real de base de datos (`409` vía `GlobalExceptionHandler`) efectivamente
ocurre, un test necesita que el `INSERT` llegue de verdad a Postgres *durante* el request — y bajo rollback
compartido eso no pasa (ver el detalle técnico en el gotcha de flush más abajo). `AdminTagControllerTest.
createWithDuplicateNameReturnsConflict` es el único test que marca `@Transactional(propagation =
Propagation.NOT_SUPPORTED)` a nivel de método para ese caso puntual, y limpia manualmente la fila creada en un
`finally`.

**Valores únicos:** como la base compartida ya tiene contenido real (es la misma de producción), cualquier
`slug`/`name`/`company` que un test cree debe ser aleatorio (`"test-" + UUID.randomUUID()`) para no chocar con
datos reales ni con los `UNIQUE` que `GlobalExceptionHandler` mapea a `409`.

## Autenticación en los tests

La mayoría de los tests no llaman a `POST /api/v1/auth/login` — en su lugar autowirean `JwtService` y generan un
token directo:

```java
String token = jwtService.generate("test-admin", "ADMIN").token();
```

`JwtAuthenticationFilter` solo valida la firma/claims del JWT, nunca consulta la base de datos, así que un
username inventado sirve igual para probar las reglas de autorización. El login real (usuario/password contra el
admin sembrado) solo se prueba en `AuthControllerTest`, usando `ADMIN_USERNAME`/`ADMIN_PASSWORD` del entorno.

## R2 mockeado, no real

`AdminMediaControllerTest` reemplaza el bean `S3Client` con `@MockitoBean` — los tests verifican que se llama a
`putObject`/`deleteObject` con los parámetros esperados, sin red ni credenciales reales, y sin dejar archivos de
prueba en el bucket real.

## Gotchas de Spring Boot 4.1 / Hibernate 7 encontrados escribiendo esta suite

- **`@AutoConfigureMockMvc`** vive ahora en `org.springframework.boot.webmvc.test.autoconfigure` (Boot 4.1 movió
  varios paquetes de test-autoconfigure; mismo patrón que la exclusión de `UserDetailsServiceAutoConfiguration`
  documentada en CLAUDE.md).
- **`@MockBean` fue eliminado** en Boot 4.0 — usar `@MockitoBean` de
  `org.springframework.test.context.bean.override.mockito` (ya viene transitivamente con los starters de test
  existentes, no hace falta agregar dependencia).
- **`MockMvcRequestBuilders.multipart(String, Object...)`** — los argumentos variádicos son variables de plantilla
  de URI, no archivos. Hay que encadenar `.file(...)` explícitamente o el archivo nunca se adjunta (el controller
  ve "Required part 'file' is not present" en vez de fallar por otra razón).
- **Timing de flush de JPA:** con `GenerationType.UUID` (el id se asigna en memoria en `persist()`, a diferencia
  de `IDENTITY`), `repository.save()` no manda el `INSERT` real a Postgres hasta un flush. Bajo el rollback
  compartido del test, ese flush nunca ocurre a menos que algo lo fuerce — por eso dos `POST` seguidos creando el
  mismo `name`/`slug` devuelven `201` los dos si no se resuelve este problema (ver la excepción documentada arriba
  para `createWithDuplicateNameReturnsConflict`). El mismo mecanismo explica por qué `@CreationTimestamp` puede
  quedar `null` justo después de un `save()` sin flush explícito.
- **`ResponseStatusException` no tiene body JSON por defecto** en este proyecto (`spring.mvc.problemdetails.enabled`
  no está prendido) — el mensaje solo se puede leer vía `response.getErrorMessage()`, no con `jsonPath("$.detail")`.
  Solo lo que `GlobalExceptionHandler` mapea explícitamente (como `DataIntegrityViolationException` → `409`)
  devuelve un body JSON real.
