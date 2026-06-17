<div align="center">
  <h1>Jamón al Plato 🍷</h1>
  <p><em>Sinfonía de la Dehesa. Maestría en cada trazo.</em></p>
</div>

---

## 📌 Visión del Proyecto
**Jamón al Plato** es una plataforma web (Landing Page & futura Booking App) diseñada para ofrecer una experiencia *premium* orientada a los servicios de alta gastronomía y corte de jamón. 
El desarrollo se centra en una estética sobria, minimalista y técnica, combinando rendimiento absoluto con una arquitectura de software de clase empresarial.

## 🏗 Arquitectura
Este proyecto abandona los patrones tradicionales de React y adopta un híbrido estricto entre **FSD (Feature-Sliced Design)** y **Atomic Design**.

### Jerarquía FSD
```text
src/
├── app/                # Inicialización de React, Providers globales, Router.
├── pages/              # Composición de vistas principales (LandingPage, BookingApp).
├── widgets/            # Bloques autónomos y complejos (Hero, Navbar, Filosofia, Footer).
├── features/           # Lógica de acciones de usuario (ContactForm).
├── entities/           # Datos de dominio y entidades visuales (BlogCard).
├── shared/             # Átomos, moléculas, hooks genéricos y diseño de sistema (tokens).
```

## 🎨 Sistema de Diseño
Hemos construido un sistema modular de estilos **100% independiente** (Zero Tailwind):
- **SCSS Modules:** Cada componente maneja sus estilos de forma co-localizada y encapsulada.
- **Variables CSS & Tematización Dinámica:** Implementado un sistema *Light/Dark Mode* gestionado por React Context. Un único cambio en el DOM propaga la tematización por toda la aplicación en `O(1)`.
- **Acento Vino (`#8B1C31`):** Diseñado para evocar el color característico del jamón ibérico, aportando contraste y sofisticación contra el fondo Dark/Bone.

## 🚀 Entorno Local
**Requisitos Previos:** Node.js v18+

1. **Instalar dependencias:**
   ```bash
   npm install
   ```
2. **Ejecutar servidor de desarrollo:**
   ```bash
   npm run dev
   ```
3. **Construir para producción (Type-checked):**
   ```bash
   npm run build
   ```

## 🛠 Tech Stack
- **Framework:** React 18 + TypeScript + Vite.
- **Estilos:** SCSS Modules + Variables CSS.
- **Enrutamiento:** React Router DOM (v6).
- **Animaciones:** Motion (Framer Motion).
- **Iconografía:** Lucide React.
- **Validación:** Zod (Preparado para la ingesta de CMS/Markdown).

> *Para conocer la evolución histórica de las decisiones de diseño y arquitectura, consulta el [Diario de Desarrollo (DEV_LOG.md)](./DEV_LOG.md).*
