# ==============================================================================
# Dockerfile para Frontend JavaFX en Entorno de Desarrollo (X11 Forwarding)
# ==============================================================================
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Instalar Maven y las librerías gráficas GTK/X11 requeridas por JavaFX en Debian
RUN apt-get update && apt-get install -y \
    maven \
    libgtk-3-0 \
    libxtst6 \
    libxxf86vm1 \
    libglu1-mesa \
    libgl1 \
    libglx-mesa0 \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxi6 \
    libasound2t64 \
    && rm -rf /var/lib/apt/lists/*

# Copiar el pom root parental (necesario ya que pom-frontend.xml hereda de él)
COPY pom.xml ./pom.xml

# Copiar el pom de definición del frontend
COPY frontend/frontend/pom-frontend.xml ./frontend/frontend/pom-frontend.xml

# Descargar las dependencias y plugins en modo offline (optimización de caché)
RUN mvn -Dmaven.repo.local=/app/.m2 -f frontend/frontend/pom-frontend.xml dependency:go-offline -B

# Copiar el código fuente del frontend
COPY frontend/frontend/src ./frontend/frontend/src

# Compilar el código fuente, copiar dependencias y configurar el usuario no-root en una única instrucción RUN para optimizar capas
RUN mvn -Dmaven.repo.local=/app/.m2 -f frontend/frontend/pom-frontend.xml clean compile && \
    mvn -Dmaven.repo.local=/app/.m2 -f frontend/frontend/pom-frontend.xml dependency:copy-dependencies && \
    if getent passwd ubuntu; then userdel -f ubuntu; fi && \
    if getent group ubuntu; then groupdel ubuntu; fi && \
    groupadd -g 1000 developer && \
    useradd -u 1000 -g 1000 -m developer && \
    chown -R developer:developer /app

USER developer

# Comando por defecto para arrancar la interfaz gráfica directamente con Java (evitando el fork de Maven)
CMD ["java", \
     "-Dprism.order=sw", \
     "-Dprism.verbose=true", \
     "--module-path", "frontend/frontend/target/dependency:frontend/frontend/target/classes", \
     "--module", "com.hambooking.frontend/com.hambooking.frontend.HamBookingApp"]
