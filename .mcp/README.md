# Guía de Uso del MCP Server - HamBooking 🥩 (Evolucionado)

Este servidor MCP local proporciona herramientas específicas a la IA para acelerar el desarrollo, auditoría, pruebas y la **dockerización** del proyecto **HamBooking**. A continuación, se detalla cómo registrar el servidor y usarlo de forma óptima en tu flujo de trabajo.

---

## 🚀 1. Configuración y Activación

### En el IDE Antigravity (y extensiones compatibles como Cline / RooCode)
El IDE leerá automáticamente el archivo de configuración `.vscode/cline_mcp.json` o `mcp.json` al abrir el proyecto `/home/sergio/vscode/java-dam`.

Si necesitas configurar el servidor MCP de forma manual en tu cliente, agrega la siguiente entrada en la sección de servidores MCP:

```json
{
  "mcpServers": {
    "hambooking-mcp": {
      "command": "node",
      "args": ["/home/sergio/vscode/java-dam/.mcp/index.js"],
      "env": {}
    }
  }
}
```

*Nota:* El wrapper ejecutable `.mcp/run.sh` puede usarse directamente como comando si prefieres levantar el script de bash.

---

## 🛠️ 2. Flujo de Trabajo Recomendado (Bucle de Calidad)

Este MCP está diseñado para usarse como un **bucle de retroalimentación de calidad** (Feedback Loop) durante el desarrollo, refactorización y despliegue.

### Paso A: Inspección y Contexto Inicial
Antes de modificar una clase de lógica de negocio o un controlador, pídele a la IA que use el MCP para comprender el diseño actual del sistema.
*   **Prompt ejemplo:** *"Usa la herramienta `read_project_docs` para ver el diseño del modelo ER y los casos de uso principales. Luego, usa `analyze_java_class` para cargar la clase `ReservationService`."*

### Paso B: Consulta a la Documentación Oficial (Docker, Spring Boot, etc.)
Consulta las directrices más actualizadas en caliente. Si estás sin conexión (entornos aislados como el sandbox), el MCP recurrirá automáticamente al fallback offline de la memoria local en `memory.json`.
*   **Prompt ejemplo:** *"Usa `fetch_official_docs` para buscar las mejores prácticas de 'multi-stage' en el tema 'docker'."*

### Paso C: Auditoría Clean Code frente a Mandatos (Java y TypeScript/React)
Antes de realizar cualquier cambio, audita el archivo o componente para identificar deudas técnicas y asegurar el cumplimiento de `GEMINI.md` y las reglas FSD.
*   **Para Java:** *"Ejecuta `check_clean_code` en la entidad `Reservation` y dime si cumple con todas las reglas de Javadoc, comentarios inline y FetchType.LAZY."*
*   **Para React/TypeScript:** *"Ejecuta `check_clean_code` en el componente `Logo` o `Navbar` para verificar reglas FSD, ausencia de 'any' e importaciones relativas permitidas."*

### Paso D: Generación de Casos de Prueba (JUnit 5 / Vitest)
Si vas a refactorizar o agregar lógica, asegura la suite de pruebas. Si la clase no tiene suficientes tests, pídele al MCP que diseñe la estructura de pruebas.
*   **Para Java:** *"Usa `propose_java_tests` para la clase `AvailabilityService` y genera la estructura básica en JUnit 5 y Mockito para sus métodos."*
*   **Para React:** *"Usa `propose_web_tests` para el componente `Logo` y genera el esqueleto de pruebas para Vitest y React Testing Library."*

### Paso E: Dockerización Inteligente y Rápida
Genera configuraciones óptimas de despliegue analizando la memoria local y las mejores prácticas acumuladas (incluyendo el nuevo frontend React).
*   **Prompt ejemplo:** *"Ejecuta `propose_docker_configs` para obtener las plantillas multi-stage del backend, frontend JavaFX, base de datos MySQL y frontend web React."*

### Paso F: Refactorización y Verificación Automática (Blindaje)
Aplica los cambios en el código. Al finalizar una iteración de refactorización, pídele a la IA que verifique la suite completa de forma automatizada.
*   **Para Java:** *"He terminado de refactorizar. Ejecuta `run_maven_tests` para comprobar que la suite de pruebas unitarias sigue pasando en verde."*
*   **Para React:** *"Ejecuta `run_web_tests` para verificar que todas las pruebas de Vitest del frontend web están pasando."*

---

## 💡 3. Herramientas Web Incorporadas

- `run_web_tests`: Ejecuta `vitest run` de forma no interactiva para validar el frontend React en el directorio `frontend-web`.
- `propose_web_tests`: Proporciona una plantilla de testing robusta con Vitest y React Testing Library adaptada a la lógica del componente React analizado.
- `check_clean_code`: Extendido para analizar archivos `.ts` y `.tsx`. Revisa:
  1. No usar comentarios inline (`//`) para documentación primaria.
  2. No usar el tipo prohibido `any`.
  3. No usar importaciones relativas hacia directorios superiores (`../`).
  4. Respetar estrictamente la jerarquía descendente FSD (`app > pages > widgets > features > entities > shared`).
