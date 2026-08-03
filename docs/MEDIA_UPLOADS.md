# media_assets y Cloudflare R2

`media_assets` guarda solo metadata (`filename`, `url`, `contentType`, `sizeBytes`, `uploadedAt`) — los archivos en
sí viven en un bucket de Cloudflare R2 (S3-compatible). Otros recursos (`Certification.imageUrl`,
`SiteProfile.cvUrl`, `Project.coverImageUrl`, etc.) no tienen FK a `media_assets`: son campos de texto planos, así
que el flujo es subir el archivo por `POST /api/v1/admin/media`, copiar la URL pública que devuelve, y pegarla a
mano en el campo correspondiente al crear/editar ese otro recurso.

R2 se eligió sobre Supabase Storage porque el sitio público (`portfolio`, Astro) ya vive en Cloudflare Pages con el
dominio `rafotijero.dev` — consolida todo en una sola cuenta.

## Crear el bucket y obtener las credenciales

1. **Activar R2** — en el [dashboard de Cloudflare](https://dash.cloudflare.com), sección **R2 Object Storage**.
   La primera vez pide un método de pago (tiene free tier, pero exige tarjeta para activarlo).
2. **Crear el bucket** — `Create bucket`, nombre libre (ej. `portfolio-cms-media`). Ese nombre es tu
   `R2_BUCKET_NAME`.
3. **`R2_ACCOUNT_ID`** — visible en la pestaña **General** del bucket, en el endpoint "S3 API":
   `https://{account_id}.r2.cloudflarestorage.com`.
4. **Credenciales de API** — `Manage R2 API Tokens` → `Create API Token`, permisos **Object Read & Write**
   (idealmente con scope limitado a este bucket). Te muestra **Access Key ID** y **Secret Access Key** una sola
   vez — son `R2_ACCESS_KEY_ID` y `R2_SECRET_ACCESS_KEY`.
5. **URL pública (`R2_PUBLIC_URL`)** — en `Settings` del bucket:
   - Rápido/dev: activar **Public Development URL** (`*.r2.dev`) — no recomendado para producción (rate limits,
     sin garantías de caché).
   - Producción (usado acá): **Custom Domains** → `Connect Domain`, con un subdominio del dominio que ya esté en
     la misma cuenta de Cloudflare (ej. `media.rafotijero.dev`). El DNS/SSL se provisionan automáticamente porque
     la zona ya es de la cuenta — no hace falta registrar el subdominio en ningún otro lado, los subdominios no se
     registran por separado, son solo registros DNS dentro de la zona que ya controlás.

### Gotcha: "The specified zone id is not valid"

Si al hacer `Connect Domain` sale este error aunque el dominio esté `Active` y en la cuenta correcta: **el campo
espera el hostname sin protocolo**. Escribir `https://media.rafotijero.dev` rompe la resolución de la zona;
tiene que ser `media.rafotijero.dev` a secas, sin `https://` ni `/` al final.

## Configuración de la app

Los 5 valores van en `.env.local` (gitignored) / variables de entorno de Render, leídos en `application.yaml` bajo
la key `r2.*` — ver [README.md](../README.md#configuración).

## Implementación

- `config/R2Config.java` — bean `S3Client` apuntando al endpoint de R2, con `pathStyleAccessEnabled(true)`.
- `service/MediaAssetService.java` — sube con key `${uuid}-${nombre-sanitizado}`, arma la URL pública prefijando
  `r2.public-url`, y para borrar deriva la key quitando ese mismo prefijo de la URL guardada.
- `api/AdminMediaController.java` — `GET`/`POST`/`DELETE` bajo `/api/v1/admin/media`, ya cubierto por la regla de
  seguridad existente `/api/v1/admin/**` → `ROLE_ADMIN`.
- Sin whitelist de content-type: `SiteProfile.cvUrl` también sube PDFs por este mismo endpoint, así que restringir
  a solo imágenes habría roto ese caso.
- Límite de subida: `spring.servlet.multipart.max-file-size`/`max-request-size` en 10MB (el default de Spring es
  1MB, muy chico para imágenes o el PDF del CV).

Para los tests, `AdminMediaControllerTest` mockea el `S3Client` en vez de pegarle a R2 real — ver
[docs/TESTING.md](TESTING.md).
