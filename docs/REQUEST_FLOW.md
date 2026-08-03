# Flujo de un request, Docker y despliegue

Diagrama: [diagrams/request-flow.html](diagrams/request-flow.html)

Este documento cubre lo que no entra ni en "por qué" (`ARCHITECTURE.md`) ni en "qué existe y dónde" (`CLAUDE.md`):
el recorrido real de un request de principio a fin, cómo se construye la imagen de Docker, y qué dispara un
despliegue.

## CI/CD: no existe como archivo en este repo

No hay `.github/workflows` ni `render.yaml` — no hay pipeline definido en código. El "pipeline" es la integración
nativa de Render con GitHub: Render está conectado directamente a este repositorio desde su dashboard (no desde
un archivo acá), escucha pushes a `main`, y en cada uno clona el commit, construye la imagen con el `Dockerfile` y
la despliega, reemplazando la instancia anterior.

No hay tests, linting, ni ningún gate automático antes de eso — el build de Docker es la única validación (falla
solo si el código no compila o el `Dockerfile` está roto). El historial de builds/deploys y sus logs vive en el
dashboard de Render (servicio → pestaña **Events** o **Logs**), no en este repo ni en GitHub Actions.

## `Dockerfile`: build multi-stage

```dockerfile
FROM eclipse-temurin:17-jdk AS build     # stage 1: compilar
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline
COPY src ./src
RUN ./mvnw -q clean package -DskipTests

FROM eclipse-temurin:17-jre              # stage 2: solo runtime
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- **Dos stages**: el primero tiene el JDK completo (necesario para compilar), el segundo solo el JRE — la imagen
  final no carga herramientas de build, más liviana y con menos superficie de ataque.
- **`dependency:go-offline` antes de copiar `src`**: Docker cachea capas por instrucción; si solo cambia el
  código fuente (no `pom.xml`), esta capa de dependencias se reutiliza y el build es más rápido.
- **`-DskipTests`**: los tests necesitan conectividad real a Neon (sin perfil de test separado, ver
  [`TESTING.md`](TESTING.md)), y el build de Docker no tiene esas credenciales ni debería tenerlas — los tests se
  corren aparte, nunca dentro del build de la imagen.
- **`ENTRYPOINT`** corre el jar directo — `server.port` lee `${PORT:8080}` porque Render inyecta el puerto real
  en runtime vía variable de entorno, no siempre `8080`.

## Flujo de un request — caso público (sin auth)

`GET /api/v1/posts/{slug}`:

1. **`DispatcherServlet`** (Spring MVC) recibe el request.
2. **`SecurityFilterChain`** (`config/SecurityConfig.java`) lo intercepta primero — la regla
   `.requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()` lo deja pasar sin pedir token.
3. Llega a `PostController.detail(slug)` (`api/PostController.java`).
4. El controller delega a `PostService.findPublishedBySlug(slug)` (`service/PostService.java`) — método
   `@Transactional(readOnly = true)` a nivel de clase.
5. El service usa `PostRepository` (Spring Data JPA) para traer la entidad `Post`, filtra que esté `PUBLISHED`, y
   si no existe o no lo está, lanza `ResponseStatusException(404)`.
6. Mapea la entidad a `PostDetailDto` (`api/dto/PostDetailDto.java`) **dentro** de la transacción — necesario
   porque `Post.tags` es una relación lazy y `open-in-view` está deshabilitado; si el mapeo pasara fuera de la
   transacción, tiraría `LazyInitializationException`.
7. Jackson serializa el DTO a JSON, Spring MVC lo devuelve como respuesta `200`.

## Flujo de un request — caso admin (con auth y escritura)

`POST /api/v1/admin/posts` con `Authorization: Bearer <token>`:

1. Mismo `SecurityFilterChain`, pero ahora entra `JwtAuthenticationFilter`
   (`security/JwtAuthenticationFilter.java`), registrado con `addFilterBefore` antes del filtro estándar de
   autenticación. Lee el header, valida la firma con `JwtService`, y si es válido puebla el `SecurityContext` con
   `ROLE_ADMIN`.
2. La regla `.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")` exige esa autoridad — si no hay token o es
   inválido, corta acá con `401` (vía el `AuthenticationEntryPoint` configurado).
3. Si pasa, Spring MVC corre **Bean Validation** sobre `PostRequest` (`@NotBlank`, `@NotNull` en los campos)
   antes de invocar el controller — si falla, `400` automático.
4. `AdminPostController.create()` → `PostService.create()` (`@Transactional`, ya no `readOnly`): resuelve
   `tagIds` contra `TagRepository` (si alguno no existe, `400` explícito), guarda la entidad, mapea a DTO.
5. Si hay un choque de `UNIQUE` (slug duplicado), la excepción de JPA sube hasta `GlobalExceptionHandler`
   (`api/GlobalExceptionHandler.java`), que la traduce a `409` en vez del `500` que tiraría por defecto.
6. Respuesta `201` con el DTO creado.
