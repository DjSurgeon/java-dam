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

### Paso C: Auditoría Clean Code frente a Mandatos
Antes de realizar cualquier cambio, audita la clase actual para identificar deudas técnicas y asegurar el cumplimiento de `GEMINI.md`.
*   **Prompt ejemplo:** *"Ejecuta `check_clean_code` en la entidad `Reservation` y dime si cumple con todas las reglas de Javadoc, comentarios inline y FetchType.LAZY."*

### Paso D: Generación de Casos de Prueba (JUnit 5 + Mockito)
Si vas a refactorizar o agregar lógica, asegura la suite de pruebas. Si la clase no tiene suficientes tests, pídele al MCP que diseñe la estructura de pruebas.
*   **Prompt ejemplo:** *"Usa `propose_java_tests` para la clase `AvailabilityService` y genera la estructura básica en JUnit 5 y Mockito para sus métodos."*

### Paso E: Aprendizaje Dinámico (Memoria Local)
Cuando descubras una buena práctica específica del proyecto o una lección aprendida durante la refactorización, guárdala en el archivo `.mcp/memory.json` para que esté disponible para siempre.
*   **Prompt ejemplo:** *"Usa `remember_concept` para guardar como 'project_lessons' la regla: 'Las contraseñas siempre se codifican con BCrypt antes de guardarse en el UserService', con el tag 'security'."*
*   **Para buscar en memoria:** *"Usa `recall_concepts` con el tag 'security' o buscando 'BCrypt'."*

### Paso F: Dockerización Inteligente y Rápida
Genera configuraciones óptimas de despliegue analizando la memoria local y las mejores prácticas acumuladas.
*   **Prompt ejemplo:** *"Ejecuta `propose_docker_configs` para obtener las plantillas multi-stage del backend, frontend JavaFX y base de datos MySQL."*

### Paso G: Refactorización y Verificación Automática (Blindaje)
Aplica los cambios en el código. Al finalizar una iteración de refactorización, pídele a la IA que verifique la suite completa de forma automatizada.
*   **Prompt ejemplo:** *"He terminado de refactorizar. Ejecuta `run_maven_tests` para comprobar que la suite de 496 pruebas unitarias sigue pasando en verde."*

### Paso H: Comprobaciones de Base de Datos y Datos Reales
Si necesitas validar si una regla de negocio se está insertando correctamente en caliente, puedes realizar consultas de solo lectura a la base de datos MySQL local desde el chat de la IA.
*   **Prompt ejemplo:** *"Usa `analyze_database` con la acción `query` para verificar las últimas 5 reservas guardadas en la tabla `reservations` y ver si sus estados coinciden."*

---

## 💡 3. Buenas Prácticas y Consejos

1.  **Aislamiento:** Este MCP solo lee y actúa sobre este repositorio. No se verá afectado ni contaminará otros workspaces o entornos de tu máquina.
2.  **Seguridad SQL:** La herramienta `analyze_database` cuenta con protección regex. Solo ejecutará sentencias que comiencen con `SELECT`, `SHOW`, `DESCRIBE` o `EXPLAIN`. No es posible inyectar `DROP`, `DELETE` o `UPDATE` accidentales a través del MCP.
3.  **Logs en Tiempo Real:** Las salidas estándar de error (`stderr`) del comando `./mvnw test` se transmiten en tiempo real a la consola de la IA, permitiéndote ver exactamente qué test falló sin necesidad de cambiar de terminal.
4.  **Uso de la Memoria en Git:** El archivo `.mcp/memory.json` forma parte del repositorio. Cuando la IA "aprenda" una regla y la guarde, hazle commit para que otros desarrolladores (o futuras sesiones de la IA) también tengan acceso a ese conocimiento.
