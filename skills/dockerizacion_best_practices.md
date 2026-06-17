# Skill: Dockerización de HamBooking 🐳

Este archivo es un **Skill File** de ejecución directa para la IA. Define las reglas de oro, directrices de diseño y plantillas exactas que debes seguir para dockerizar los tres componentes del proyecto (Base de Datos, Backend y Frontend con GUI JavaFX).

---

## 📋 1. Reglas de Oro Generales
1. **LIGEREZA (Alpine First)**: Siempre que sea viable, utiliza imágenes base basadas en **Alpine Linux** para minimizar el tamaño en disco, optimizar el consumo de red y acelerar el arranque.
2. **CONTROL TOTAL (Sin `:latest`)**: Queda estrictamente prohibido usar el tag `:latest`. Todas las imágenes base de compilación, ejecución y servicios auxiliares deben tener tags con versiones específicas y fijadas.
3. **CACHÉ DE CAPAS**: En todos los Dockerfiles, copia primero los archivos de definición de dependencias (`pom.xml`, etc.) y ejecuta la descarga de dependencias antes de copiar el código fuente. Esto evita reconstruir las dependencias si solo cambia el código.
4. **AISLAMIENTO**: Todos los servicios de red deben comunicarse utilizando nombres DNS internos del Docker Compose, sin exponer puertos innecesarios al exterior, salvo los de acceso obligatorio (puertos de la API y de depuración).

---

## 🗄️ 2. Base de Datos (MySQL)
- **Imagen Base**: Utilizar `mysql:8.0` o una versión específica menor de la familia 8.0 (ej. `mysql:8.0.40`).
- **Persistencia**: Montar un volumen con nombre (`db_data`) en la ruta `/var/lib/mysql`.
- **Inicialización**: Montar el script `./database/schema.sql` en `/docker-entrypoint-initdb.d/schema.sql` para que MySQL cree las tablas automáticamente al levantar por primera vez.
- **Control de Salud (Healthcheck)**:
  ```yaml
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
    interval: 10s
    timeout: 5s
    retries: 5
  ```

---

## ☕ 3. Backend (Spring Boot 3.x / Java 21)
- **Compilación (Fase 1)**: Usar `maven:3.9.9-eclipse-temurin-21-alpine` como constructor.
  - Copiar `pom.xml` a `/app` y correr `mvn dependency:go-offline -B` para descargar y cachear dependencias.
  - Copiar la carpeta `src` y correr `mvn clean package -DskipTests`.
- **Ejecución (Fase 2)**: Usar `eclipse-temurin:21.0.5_11-jre-alpine` como runtime de producción.
  - Copiar únicamente el archivo `.jar` generado de la Fase 1.
  - Ejecutar el proceso con un usuario no-root (`USER 1000:1000`) por seguridad.
  - Configurar las variables de entorno de conexión a base de datos de manera dinámica (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).

---

## 🖥️ 4. Frontend (JavaFX 21 GUI)
- **Imagen Base**: Usar una imagen Alpine específica con JavaFX integrado: `bellsoft/liberica-openjdk-alpine-with-javafx:21.0.4`.
- **X11 Forwarding (Ejecución de GUI)**:
  - Para que la ventana JavaFX se renderice en el monitor del host Linux:
    - En la configuración del servicio, inyectar la variable de entorno `DISPLAY=${DISPLAY}`.
    - Montar el socket de X11 del host en modo lectura: `/tmp/.X11-unix:/tmp/.X11-unix:ro`.
    - Usar la opción `network_mode: host` en Docker Compose para simplificar el mapeo del socket gráfico en entornos de desarrollo Linux locales.

### 🐧 Comandos del Host (Preparación obligatoria)
Antes de ejecutar `docker compose up`, el usuario debe preparar el servidor X11 del host ejecutando en su terminal local:
```bash
# Permitir conexiones del contenedor al servidor gráfico local X11
xhost +local:docker
```

---

## 🗺️ 5. Orquestación (docker-compose.yml)
- **Versión de sintaxis**: Usar versión de archivo `3.8`.
- **Orden de Arranque**:
  - El servicio `backend` debe tener una dependencia estricta con el servicio `db`, condicionada a que la base de datos esté completamente lista e iniciada:
    ```yaml
    depends_on:
      db:
        condition: service_healthy
    ```
  - El servicio `frontend` debe depender del servicio `backend`.
- **Variables de Entorno**: Almacenar las credenciales por defecto (ej: `MYSQL_ROOT_PASSWORD=sergio1234`) para simplificar el levantamiento en desarrollo rápido.

---

## 🧪 6. Checklist de Validación
Para validar si la dockerización se completó con éxito:
1. Ejecutar `mvn clean` localmente para evitar copiar residuos locales a los contextos de construcción.
2. Ejecutar `xhost +local:docker` en la terminal del host.
3. Levantar con `docker compose up --build`.
4. Verificar en consola que `hambooking-backend` conecta a `hambooking-db` y que no arroja errores de persistencia (Hibernate).
5. Confirmar que la interfaz gráfica del frontend JavaFX se abre correctamente en la pantalla del host.
