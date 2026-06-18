# Skill: Dockerización de HamBooking 🐳

Este archivo es un **Skill File** de ejecución directa para la IA. Define las reglas de oro, directrices de diseño y plantillas exactas que debes seguir para dockerizar los cuatro componentes del proyecto (Base de Datos, Backend, Frontend Desktop JavaFX y Frontend Web React).

---

## 📋 1. Reglas de Oro Generales
1. **LIGEREZA (Alpine First)**: Siempre que sea viable, utiliza imágenes base basadas en **Alpine Linux** para minimizar el tamaño en disco, optimizar el consumo de red y acelerar el arranque.
2. **CONTROL TOTAL (Sin `:latest`)**: Queda estrictamente prohibido usar el tag `:latest`. Todas las imágenes base de compilación, ejecución y servicios auxiliares deben tener tags con versiones específicas y fijadas.
3. **CACHÉ DE CAPAS**: En todos los Dockerfiles, copia primero los archivos de definición de dependencias (`pom.xml`, `package.json`, etc.) y ejecuta la descarga de dependencias antes de copiar el código fuente. Esto evita reconstruir las dependencias si solo cambia el código.
4. **AISLAMIENTO**: Todos los servicios de red deben comunicarse utilizando nombres DNS internos del Docker Compose, sin exponer puertos innecesarios al exterior, salvo los de acceso obligatorio (puertos de la API y de depuración).

---

## 🗄️ 2. Base de Datos (MySQL)
- **Imagen Base**: Utilizar `mysql:8.0` o una versión específica menor de la familia 8.0 (ej. `mysql:8.0.40`).
- **Persistencia**: Montar un volumen con nombre (`db_data`) en la ruta `/var/lib/mysql`.
- **Inicialización**: Montar el script `./database/schema.sql` en `/docker-entrypoint-initdb.d/schema.sql` usando el flag de lectura y contexto compartido (`:ro,z`) para que MySQL cree las tablas automáticamente al levantar por primera vez, garantizando la compatibilidad con sistemas con SELinux activado.
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
- **Imagen Base**: Usar la imagen oficial `eclipse-temurin:21-jdk` (basada en Ubuntu). Se requiere la instalación manual de librerías de runtime de X11 y OpenGL (`libgtk-3-0`, `libgl1`, `libglx-mesa0`, `libx11-6`, `libxext6`, `libxrender1`, `libxi6`, `libxtst6`, `libasound2t64`, etc.) debido a que JavaFX depende de ellas para cargar sus binarios nativos, siendo incompatible con entornos Alpine/musl.
- **X11 Forwarding (Ejecución de GUI)**:
  - Para que la ventana JavaFX se renderice en el monitor del host Linux de forma segura y autorizada:
    - En la configuración del servicio, inyectar las variables de entorno `DISPLAY=${DISPLAY:-:0}`, `XAUTHORITY=/home/developer/.Xauthority`, `QT_X11_NO_MITSHM=1` y `NO_AT_BRIDGE=1`.
    - Montar el socket de X11 del host: `/tmp/.X11-unix:/tmp/.X11-unix:ro,z`.
    - Montar el archivo de cookies de autorización X11 del host: `${XAUTHORITY:-~/.Xauthority}:/home/developer/.Xauthority:ro,z`.
    - Usar la opción `network_mode: host` y desactivar el etiquetado restrictivo de SELinux con `security_opt: [ "label=disable" ]` en Docker Compose.
    - **Usuario No-Root y UIDs**: Crear y usar un usuario (`USER developer` con UID/GID `1000:1000`) dentro del contenedor que coincida con el UID del usuario del host para evitar fallos de denegación de conexión por parte del servidor X11. Si la imagen base contiene un usuario por defecto (como `ubuntu` con UID 1000), este debe ser eliminado previamente.
    - **Estrategia de Ejecución en Contenedores**: Para evitar que el plugin `javafx-maven-plugin` de Maven realice un *fork* del subproceso (ocultando salidas y devorando variables de entorno), se debe compilar el código (`clean compile`) y copiar las dependencias (`dependency:copy-dependencies`) en la fase de construcción de la imagen Docker. En tiempo de ejecución (CMD), la aplicación se debe iniciar directamente con el comando `java` configurando la opción `--module-path` y pasándole el parámetro de renderizado por software `-Dprism.order=sw`.

---

## 🌐 5. Frontend Web (React + Vite 6 + TS)
- **Compilación/Ejecución Multi-Stage**:
  - **`base`**: Basado en `node:20-alpine` (LTS y compatibilidad garantizada).
    - Copiar `package.json` y `package-lock.json` y ejecutar `npm ci` para resolver dependencias de forma inmutable.
  - **`development`**: Expone el puerto `3000` y arranca `npm run dev` con soporte para Hot Module Replacement (HMR) y recarga rápida.
  - **`builder`**: Compila los estáticos optimizados mediante `npm run build`.
  - **`production`**: Servidor `nginx:stable-alpine` final ultraligero que copia `/app/dist` del constructor y lo sirve en el puerto `80`.
- **Regla de Compilador Moderno (Sass)**:
  - En Vite 6, la compilación de estilos SCSS modernos exige la instalación de la dependencia de desarrollo `sass-embedded`. Es obligatorio incluirla en el `devDependencies` de la SPA.
- **Volúmenes y Sombreado de Dependencias**:
  - Para desarrollar localmente manteniendo la compatibilidad de paquetes, el volumen de desarrollo monta `./frontend-web:/app` pero enmascara `node_modules` usando un volumen anónimo `/app/node_modules`.
  - **Limpieza de Caché**: Cada vez que se agreguen o modifiquen dependencias en `package.json`, es obligatorio eliminar el volumen anónimo obsoleto ejecutando `docker compose rm -f -s -v frontend-web` antes de reconstruir la imagen.

---

## 🗺️ 6. Orquestación (docker-compose.yml)
- **Versión de sintaxis**: Usar versión de archivo `3.8`.
- **Orden de Arranque**:
  - El servicio `backend` depende de `db` (estado `service_healthy`).
  - Tanto `frontend` como `frontend-web` dependen de `backend` (estado `service_started`).
- **Límites de Hardware (CPU y Memoria)**:
  - Para evitar que la compilación de Vite o el renderizado de JavaFX saturen la CPU del host, se configuran límites máximos de hardware en desarrollo:
    ```yaml
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 1024M
    ```

---

## 🧪 7. Checklist de Validación
1. Ejecutar `mvn clean` localmente antes de construir para evitar ruido.
2. Ejecutar `xhost +local:` en la terminal del host.
3. Levantar la aplicación web/backend: `make web` y reconstruir mediante `make rebuild-web`.
4. Verificar en consola que `hambooking-backend` conecta a `hambooking-db`.
5. Confirmar que la interfaz gráfica se abre en el host y la SPA web responde en `http://localhost:3000`.

---

## 🪲 8. Lecciones Aprendidas y Solución de Errores (Troubleshooting)

### 1. `E: Package 'libasound2' has no installation candidate`
* **Solución**: Instalar `libasound2t64` en su lugar en entornos basados en Ubuntu moderno.

### 2. `groupadd: GID '1000' already exists`
* **Solución**: Eliminar forzadamente el usuario `ubuntu` antes de crear el nuestro.

### 3. `Failed to execute goal org.openjfx:javafx-maven-plugin:run (Exit value: 1)`
* **Solución**: Compilar y extraer dependencias en Dockerfile; arrancar directamente con el binario de `java`.

### 4. `java.lang.UnsupportedOperationException: Unable to open DISPLAY`
* **Solución**: Montar volúmenes con `:ro,z`, desactivar etiquetas con `security_opt: [label=disable]`, y usar `network_mode: host`.

### 5. `Preprocessor dependency "sass-embedded" not found`
* **Causa**: Vite 6 requiere un preprocesador nativo incrustado para compilar SCSS modernos de forma óptima.
* **Solución**: Añadir `"sass-embedded"` a los `devDependencies` del proyecto.

### 6. `Device or resource busy: Dockerfile`
* **Causa**: Fallo de bloqueo al intentar manipular o eliminar directamente descriptores de archivos mientras Docker compose tiene contenedores construidos o activos.
* **Solución**: Detener el servicio antes de reubicar o reescribir archivos, o bien utilizar reescritura directa de flujo en lugar de comandos destructivos de sistema (`rm`/`mv`).
