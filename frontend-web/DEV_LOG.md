# 📖 Diario de Desarrollo (DEV_LOG)

Este documento registra la evolución histórica y las decisiones arquitectónicas del proyecto **Jamón al Plato**. Sirve como bitácora para entender el "por qué" detrás del código.

---

## 📅 Hito 1: Purga y Configuración Base
**Decisión Técnica:** El proyecto inicial dependía fuertemente de Tailwind CSS. Decidimos purgarlo por completo para tener control absoluto sobre el motor de renderizado y el ciclo de pintado del navegador.
- **Acciones Realizadas:**
  - Desinstalación de Tailwind.
  - Creación de un sistema de variables globales SCSS (`abstracts/_variables.scss`, `_mixins.scss`).
  - Implementación de un diseño "Glassmorphism" puro usando `backdrop-filter`.

## 📅 Hito 2: Adopción de FSD (Feature-Sliced Design)
**Decisión Técnica:** La escalabilidad era un problema. Estructurar el código por tipo de archivo (`components/`, `styles/`) se vuelve insostenible a largo plazo. Decidimos implementar FSD para separar el código por *responsabilidad de dominio*.
- **Acciones Realizadas:**
  - Migración de los bloques principales a la capa `widgets` (Hero, Navbar, Filosofia, Footer, BlogGrid).
  - Encapsulamiento del formulario de contacto en la capa `features/ContactForm`.
  - Configuración de alias absolutos (`@/*`) en TypeScript y Vite.

## 📅 Hito 3: Tematización Dinámica y Router SPA
**Decisión Técnica:** Necesitábamos que el sistema fuera accesible (Light Mode) sin perder su estética premium (Dark Mode con acento Vino).
- **Acciones Realizadas:**
  - Migración de las variables estáticas de SCSS a variables CSS nativas vinculadas a `:root` y `[data-theme='light']`.
  - Creación del `ThemeProvider` (React Context) para manejar el tema, con persistencia en `localStorage`.
  - Implementación del átomo animado `ThemeToggle`.
  - Configuración del enrutamiento base con `react-router-dom` para la transición fluida hacia la futura `/reservas`.
  - Abstracción de lógicas intrusivas de `framer-motion` hacia hooks reutilizables (`useScrollReveal`, `useParallax`).

## 📅 Hito 4: Atomic Design & Componentización Granular
**Decisión Técnica:** Para asegurar que la próxima fase (BookingApp) no repita código, aplicamos Atomic Design sobre la estructura FSD, abstrayendo patrones comunes.
- **Acciones Realizadas:**
  - Creación de **Moléculas** genéricas (`SectionBadge`, `FeatureCard`).
  - Extracción de **Entidades** visuales (`BlogCard` encapsulado en la capa de negocio `entities`).
  - Unificación del Sistema de Formularios mediante átomos (`Input`, `Textarea`) y moléculas (`FormField`).
  - Refactorización de átomos existentes (`Button`, `Typography`) para hacerlos polimórficos (`as={Link}`), optimizando el SEO y la semántica HTML.

## 📅 Hito 5: Dockerización y Arquitectura de Infraestructura
**Decisión Técnica:** Para garantizar la reproducibilidad del entorno en cualquier plataforma (Fedora, Windows 11 + WSL2, VPS Linux) y preparar el proyecto para escalar hacia el backend de reservas y la tienda, se dockerizó el ecosistema completo con una arquitectura multi-servicio progresiva.

- **Acciones Realizadas:**
  - Creación de un `Dockerfile` **multi-stage** (4 etapas: `base` → `development` → `builder` → `production`) con imagen final Nginx Alpine de ~15MB.
  - Creación de `nginx.conf` con SPA routing (fallback a `index.html` para React Router), caché agresiva de assets con hash de Vite, y compresión Gzip.
  - Creación de `docker-compose.yml` (desarrollo local, puerto 3000 con HMR) con los servicios futuros (`api`, `db`, `cache`) ya definidos y comentados, listos para activar por fases.
  - Creación de `docker-compose.prod.yml` con Caddy como proxy inverso para SSL automático via Let's Encrypt.
  - Creación de `Caddyfile.example` como plantilla de configuración de dominio para el despliegue en VPS.
  - Creación de `.dockerignore` para excluir `node_modules`, `.env`, `dist` y artefactos temporales del contexto de build.
  - Reescritura completa de `HOW_TO_USE.md` con la guía completa de desarrollo, producción, roadmap de escalabilidad y deuda técnica.

- **Decisiones arquitectónicas clave:**
  - **Puerto 3000:** Se mantiene el puerto configurado en `vite.config.ts` para no romper el workflow existente.
  - **Servicios comentados:** Los bloques de `api`, `db` y `cache` en `docker-compose.yml` actúan como "contratos de interfaz" — la arquitectura del compose ya conoce los servicios aunque no existan aún.
  - **Deuda técnica identificada:** `express` está en `dependencies` del raíz y contamina el bundle de Vite. Se migrará a `/api/package.json` cuando arranque la fase BookingApp.

## 📅 Hito 6: Automatización de Pipeline (CI/CD)
**Decisión Técnica:** Eliminar el error humano en el despliegue y asegurar que solo el código que cumple con los estándares de calidad (`lint` y `test`) llegue a producción.

- **Acciones Realizadas:**
  - Implementación de **GitHub Actions** (`.github/workflows/ci-cd.yml`).
  - Configuración de un pipeline de dos etapas:
    - **Build & Push**: Verificación de tipos (`tsc`), ejecución de pruebas (`vitest`), construcción de imagen Docker de producción y subida a **GHCR** (GitHub Container Registry).
    - **Deploy**: Despliegue automático en el VPS mediante SSH, ejecutando `docker compose pull` y `up -d` para garantizar tiempo de inactividad casi nulo (zero-downtime aproximado).
  - Actualización de `docker-compose.prod.yml` para soportar la descarga de imágenes pre-construidas desde el registro.
  - Documentación completa en `HOW_TO_USE.md` sobre secretos de GitHub y flujo de despliegue.

- **Decisiones arquitectónicas clave:**
  - **GHCR sobre Docker Hub:** Mayor integración con GitHub Actions y permisos más granulares vinculados al repositorio.
  - **SSH Deploy Script:** Uso de `appleboy/ssh-action` para una ejecución segura de comandos en el servidor remoto.
  - **Image Pruning:** Inclusión de `docker image prune -f` en el script de despliegue para evitar que el almacenamiento del VPS se llene con imágenes antiguas.

---

## 🚀 Estado Actual y Próximos Pasos

### Estado Actual:
El repositorio cuenta con un **Pipeline de CI/CD profesional completamente configurado**. El código es validado, testeado, dockerizado y desplegado automáticamente al hacer push a la rama `main`. La arquitectura de infraestructura es ahora un sistema "push-to-deploy".

### Próxima Fase Lógica (Roadmap):
1. **Desarrollo de la BookingApp (`/reservas`):**
   - Crear `/api` como servicio Node/Express independiente (con su propio `package.json`).
   - Descomentar los bloques `api`, `db` y `cache` en `docker-compose.yml`.
   - Migrar `express` y `dotenv` del raíz a `/api/package.json` (deuda técnica pendiente).
   - Utilizar los átomos de formulario estandarizados para el wizard de reservas.

2. **Hidratación Dinámica de Contenido:**
   - Conectar `contentLoader.ts` a archivos Markdown reales o CMS Headless (Supabase/Sanity).

3. **Despliegue Final en VPS:**
   - Configurar DNS del dominio → IP del VPS.
   - Renombrar `Caddyfile.example` → `Caddyfile` con el dominio real.
