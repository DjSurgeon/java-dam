# Skill: Arquitectura Frontend FSD y Clean Code ⚛️

Este archivo es un **Skill File** de ejecución directa para la IA. Define las directrices arquitectónicas, los mandatos de código limpio (Clean Code) y los patrones obligatorios para construir e integrar el frontend web (**React + TypeScript + Vite**) del proyecto **HamBooking** siguiendo la fusión de **Feature-Sliced Design (FSD)** y **Atomic Design**.

---

## 📋 1. Reglas de Oro del Frontend y FSD

1. **Jerarquía de Capas Estricta**: Las importaciones siempre deben ser en sentido **descendente**. Queda terminantemente prohibido importar elementos de una capa superior.
   * `app` ➔ inicialización global, proveedores, enrutador.
   * `pages` ➔ composición de vistas principales (LandingPage, BookingApp).
   * `widgets` ➔ bloques autónomos y complejos de la interfaz (Hero, Navbar, Footer).
   * `features` ➔ lógica interactiva que realiza acciones de usuario (ContactForm, Auth).
   * `entities` ➔ datos de dominio y componentes asociados (BlogCard, UserProfile).
   * `shared` ➔ átomos, moléculas, utilidades, hooks genéricos y estilos base.

2. **API Pública Obligatoria**: Cada slice (carpeta dentro de una capa) debe exponer un archivo `index.ts` que actúe como su API Pública.
   * Cualquier archivo fuera de ese slice **debe** importar exclusivamente desde su `index.ts` de la API Pública.
   * Se prohíbe la importación interna (ej. `import { Logo } from '@/shared/ui/atoms/Logo/ui/Logo'`). Lo correcto es `import { Logo } from '@/shared/ui/atoms/Logo'`.

3. **Alias Absolutos**: Todas las importaciones internas entre componentes o capas deben utilizar el alias absoluto `@/` (definido en `tsconfig.json` y `vite.config.ts`).

---

## 🏗️ 2. Estructura Interna de un Slice (FSD + Atomic Design)

Cada componente o slice de desarrollo debe estructurarse siguiendo esta división interna de responsabilidades:

```text
nombre-del-slice/
├── index.ts              # API Pública (Exporta lo necesario de ui, model, api)
├── ui/                   # Componentes de presentación (Visual/TSX y SCSS Modules)
│   ├── NombreComponente.tsx
│   └── NombreComponente.module.scss
├── model/                # Lógica del slice, Zustand stores, tipos, constantes, selectores
│   ├── store.ts
│   ├── types.ts
│   └── selectors.ts
└── api/                  # Servicios de comunicación (llamadas a la API del backend, adaptadores)
    └── services.ts
```

---

## 💅 3. Estándares de SCSS Modules y Estilos

1. **Cero Literales Hardcodeados**: No se permite el uso de colores, tipografías o espaciados en bruto. Todos los valores deben proceder del sistema de diseño (tokens).
2. **Uso de Variables de Estilo**: Importar y emplear las variables globales usando:
   ```scss
   @use '@/shared/styles/abstracts' as v;
   
   .mi-clase {
     color: v.$color-brand-wine;
     font-family: v.$font-primary;
   }
   ```
3. **Encapsulación CSS Modules**: Cada componente visual debe tener su propio archivo `*.module.scss` co-localizado en la carpeta `ui/`.
4. **Nomenclatura**: Las clases de CSS Modules deben escribirse estrictamente en formato **kebab-case**.
5. **Profundidad de Anidamiento**: El anidamiento en SCSS **no debe superar las 3 capas** de profundidad para mantener especificidad limpia y alto rendimiento de renderizado.

---

## 🏷️ 4. Estándares de TypeScript y Estado (Zustand)

1. **Tipado Estricto (Strict Mode)**: Queda totalmente prohibido el uso del tipo `any`. Todo parámetro, retorno y estado debe ser tipado de forma explícita.
2. **Tipos de Datos de API**: Toda información proveniente del backend debe ser tratada como de solo lectura (`readonly`).
3. **Modelado**: Utilizar `interface` para definir objetos estructurados y componentes React, reservando `type` para uniones de tipos, alias o tipos primitivos combinados.
4. **Zustand (State Management)**:
   * Se mantendrá **una única tienda (store) de Zustand por cada slice** que requiera estado compartido.
   * La tienda nunca debe ser consumida directamente en los componentes visuales de la carpeta `ui/`. Debe ser consumida a través de hooks personalizados creados dentro de la carpeta `model/` para mantener el encapsulamiento.

---

## ♿ 5. Accesibilidad (A11y) y a11y Standards

1. **Guías WCAG 2.1 AA**: Todos los componentes interactivos deben ser completamente accesibles mediante teclado, contar con el contraste cromático adecuado y los roles HTML5 correspondientes.
2. **Idioma**: Tanto el contenido como las etiquetas `aria-label`, descripciones de imágenes y roles especiales deben escribirse en **Inglés**.

---

## ✍️ 6. Estándares de Documentación de Código

### 6.1. Cabecera de Archivo (Obligatoria)
Cada archivo de código (`.ts`, `.tsx`, `.scss`) debe comenzar con un encabezado descriptivo en formato JSDoc/Javadoc:

```tsx
/**
 * @file NombreDelArchivo.tsx
 * @description Explicación corta y concisa del propósito de este archivo.
 * @date YYYY-MM-DD
 * @lastUpdate YYYY-MM-DD
 * @author Sergio
 */
```

### 6.2. Documentación de Lógica Interna
Cuando programes algoritmos complejos, flujos de estado o lógica de control, documenta el código usando los siguientes prefijos para permitir la auditoría estática rápida:
* `// 💡 LOGIC:` Explicación del porqué de un algoritmo o comportamiento del estado.
* `// ♿ A11Y:` Decisiones y consideraciones tomadas para la accesibilidad.
* `// ⚡ PERF:` Razones de optimización de rendimiento (ej. uso de `useMemo`, `React.lazy`).
* `// ⚠️ NOTE:` Advertencias de dependencias, efectos secundarios o comportamientos.

---

## 🧪 7. Checklist de Validación para el Desarrollador Senior IA

Antes de dar una tarea por finalizada, debes seguir este proceso quirúrgico de validación utilizando las herramientas MCP y Docker:

1. **Auditoría de Mandatos**:
   * Ejecuta `check_clean_code` (del MCP) sobre el componente/clase que acabas de desarrollar para validar el cumplimiento de Javadoc, tipo `any`, comentarios inline y alineación arquitectónica FSD.
2. **Generación de Pruebas**:
   * Si es un componente interactivo o lógica pura de negocio, utiliza `propose_web_tests` para crear el borrador de pruebas automatizadas.
   * Crea el archivo de tests correspondiente (`.test.tsx`) co-localizado o en la suite de pruebas.
3. **Ejecución y Testing**:
   * Ejecuta `run_web_tests` desde el chat para asegurar que todas las pruebas en Vitest pasen correctamente en verde.
4. **Construcción en Docker**:
   * Valida que no existan errores de transpilación TypeScript compilando el frontend mediante el comando del Makefile:
     ```bash
     make rebuild-web
     ```
