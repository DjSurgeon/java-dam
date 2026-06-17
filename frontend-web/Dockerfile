##
# @file Dockerfile
# @description Multi-stage build para Jamón al Plato.
#              Arquitectura de 4 etapas diseñada para escalar:
#                - base:        Dependencias comunes (node:20-alpine + npm ci)
#                - development: Dev server de Vite con HMR en puerto 3000
#                - builder:     Build de producción (genera /dist optimizado)
#                - production:  Nginx Alpine sirve los estáticos (~15MB de imagen final)
#
# @date 2026-05-09
# @lastUpdate 2026-05-09
# @author Sergio
##

# =============================================================================
# ETAPA 1: BASE — Dependencias compartidas entre dev y prod
# =============================================================================
# ⚡ PERF: Usamos Alpine para minimizar el tamaño de la imagen base.
#          node:20-alpine por LTS y compatibilidad con todas las deps actuales.
FROM node:20-alpine AS base

WORKDIR /app

# 💡 LOGIC: Copiamos SOLO los manifiestos de dependencias primero.
#           Docker cachea esta capa. Si package.json no cambia,
#           no reinstala dependencias en cada build → builds mucho más rápidos.
COPY package.json package-lock.json ./

# ⚠️ NOTE: Usamos npm ci (clean install) en lugar de npm install para:
#          - Respetar el lockfile exactamente (reproducibilidad total)
#          - Fallar si hay discrepancias entre package.json y package-lock.json
RUN npm ci

# =============================================================================
# ETAPA 2: DEVELOPMENT — Servidor Vite con Hot Module Replacement
# =============================================================================
FROM base AS development

# Copiamos el código fuente completo
COPY . .

# ⚠️ NOTE: Puerto 3000 es el configurado en package.json → "vite --port=3000 --host=0.0.0.0"
#          El flag --host=0.0.0.0 ya está en el script, es imprescindible para
#          que Vite responda desde dentro del contenedor hacia el host.
# ⚠️ NOTE: En desarrollo, el proceso corre como root dentro del contenedor.
#          El UID efectivo se controla desde docker-compose.yml via "user: UID:GID"
#          para que el proceso tenga acceso al volumen montado del host.
EXPOSE 3000

# 💡 LOGIC: npm run dev ya incluye --port=3000 --host=0.0.0.0 in package.json
CMD ["npm", "run", "dev"]

# =============================================================================
# ETAPA 3: BUILDER — Compilación optimizada de producción
# =============================================================================
FROM base AS builder

COPY . .

# ⚡ PERF: vite build genera chunks con hash en /dist.
#          TypeScript es validado en esta fase (tsc --noEmit está en el pipeline).
RUN npm run build

# =============================================================================
# ETAPA 4: PRODUCTION — Nginx Alpine sirve los estáticos
# =============================================================================
# ⚡ PERF: La imagen final NO incluye Node.js, npm, ni código fuente.
#          Solo los estáticos compilados + Nginx. Resultado: ~15MB.
FROM nginx:stable-alpine AS production

# Limpiamos la web de demostración de Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copiamos el resultado del build de Vite
COPY --from=builder /app/dist /usr/share/nginx/html

# Copiamos nuestra configuración de Nginx (SPA routing + gzip + caché)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# ⚠️ NOTE: "daemon off" mantiene Nginx en primer plano para que Docker
#           pueda gestionar el proceso correctamente.
CMD ["nginx", "-g", "daemon off;"]
