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
- **Inicialización**: Montar el script `./database/schema.sql` en `/docker-entrypoint-initdb.d/schema.sql` usando el flag de lectura y contexto compartido (`:ro,z`) para que MySQL cree las tablas automáticamente al levantar por primera vez, garantizando la compatibilidad con sistemas con SELinux activado (como Fedora, RHEL, CentOS).
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
    - Usar la opción `network_mode: host` y desactivar el etiquetado restrictivo de SELinux con `security_opt: [ "label=disable" ]` en Docker Compose para simplificar y autorizar el mapeo del socket gráfico en entornos de desarrollo Linux locales.
    - **Usuario No-Root y UIDs**: Crear y usar un usuario (`USER developer` con UID/GID `1000:1000`) dentro del contenedor que coincida con el UID del usuario del host para evitar fallos de denegación de conexión por parte del servidor X11. Si la imagen base contiene un usuario por defecto (como `ubuntu` con UID 1000), este debe ser eliminado previamente con `userdel -f ubuntu` y `groupdel ubuntu` para evitar errores durante el proceso de construcción.
    - **Estrategia de Ejecución en Contenedores**: Para evitar que el plugin `javafx-maven-plugin` de Maven realice un *fork* del subproceso (ocultando salidas y devorando variables de entorno), se debe compilar el código (`clean compile`) y copiar las dependencias (`dependency:copy-dependencies`) en la fase de construcción de la imagen Docker. En tiempo de ejecución (CMD), la aplicación se debe iniciar directamente con el comando `java` configurando la opción `--module-path` y pasándole el parámetro de renderizado por software `-Dprism.order=sw`.

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
2. Ejecutar `xhost +local:` en la terminal del host.
3. Levantar pasando la variable gráfica: `DISPLAY=$DISPLAY docker compose up --build`.
4. Verificar en consola que `hambooking-backend` conecta a `hambooking-db` y que no arroja errores de persistencia (Hibernate).
5. Confirmar que la interfaz gráfica del frontend JavaFX se abre correctamente en la pantalla del host y se cierra de forma limpia (código de salida 0).

---

## 🪲 7. Lecciones Aprendidas y Solución de Errores (Troubleshooting)

A continuación, se catalogan los errores de infraestructura y entorno reales que encontramos durante la implementación y sus respectivas correcciones definitivas:

### 1. `E: Package 'libasound2' has no installation candidate`
* **Causa**: Las nuevas versiones de imágenes base (como Ubuntu 24.04 LTS en las que se basan los JDKs modernos) han renombrado el paquete heredado `libasound2` a `libasound2t64` debido a la transición del sistema para soportar tiempos de 64 bits (`time_t`).
* **Solución**: En el `Dockerfile` del frontend, instalar `libasound2t64` en su lugar.

### 2. `groupadd: GID '1000' already exists`
* **Causa**: Las imágenes base modernas de Eclipse Temurin para Java 21 vienen con un usuario preexistente llamado `ubuntu` que ya ocupa el UID y GID `1000`. Al intentar aprovisionar nuestro usuario `developer:1000`, la construcción falla.
* **Solución**: Eliminar de forma forzada el usuario y grupo preexistente antes de crear el nuestro:
  ```dockerfile
  if getent passwd ubuntu; then userdel -f ubuntu; fi && \
  if getent group ubuntu; then groupdel ubuntu; fi && \
  groupadd -g 1000 developer && \
  useradd -u 1000 -g 1000 -m developer
  ```

### 3. `Failed to execute goal org.openjfx:javafx-maven-plugin:run (Exit value: 1)`
* **Causa**: El plugin de openjfx de Maven bifurca (fork) la ejecución de la JVM. Este nuevo proceso hijo no hereda las variables de socket X11 y de red gráfica del contenedor, crasheando silenciosamente y tragándose la traza de excepción real.
* **Solución**: Separar la fase de construcción de la ejecución. Compilar (`mvn clean compile`) y extraer dependencias (`mvn dependency:copy-dependencies`) en el `Dockerfile`. En tiempo de ejecución (`CMD`), arrancar la aplicación de manera directa invocando al binario `java` sobre el `--module-path`.

### 4. `java.lang.UnsupportedOperationException: Unable to open DISPLAY`
* **Causa**: En sistemas del host con **SELinux activo** (como Fedora, RHEL, CentOS), el demonio de Docker bloquea por defecto la comunicación inter-proceso (IPC) y la lectura/escritura del contenedor hacia el socket de X11 en `/tmp/.X11-unix` y el archivo de cookies `.Xauthority`.
* **Solución**: 
  1. Montar los volúmenes con banderas de relabeling compartido de SELinux (`:ro,z`).
  2. En `docker-compose.yml`, deshabilitar específicamente el confinamiento de etiquetas de SELinux para el contenedor del frontend agregando la directiva:
     ```yaml
     security_opt:
       - label=disable
     ```
  3. Ejecutar `xhost +local:` en el host y arrancar el stack pasando explícitamente la variable de display activa: `DISPLAY=$DISPLAY docker compose up`.

### 5. `Merge this RUN instruction with the consecutive ones (docker:S7031)`
* **Causa**: SonarQube y los linters de Docker marcan como error tener sentencias `RUN` consecutivas no separadas por otras directivas, ya que crean capas de imagen innecesarias.
* **Solución**: Agrupar todas las ejecuciones lógicas continuas (como compilar, copiar dependencias y gestionar usuarios) dentro de un único bloque `RUN` encadenado con operadores `&& \`.

### 6. `No inputs were found in config file 'tsconfig.json'`
* **Causa**: TypeScript lanza un error crítico si detecta un archivo de configuración `tsconfig.json` en directorios de utilidades (como `.mcp/`) pero no encuentra directorios de código fuente TypeScript (`src/`) válidos o con archivos `.ts` en su interior.
* **Solución**: Añadir un archivo dummy como `.mcp/src/index.ts` para satisfacer las rutas definidas en el config y resolver la advertencia en el IDE.
