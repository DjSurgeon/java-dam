# ==============================================================================
# Fase 1: Compilación (Maven + JDK Alpine)
# ==============================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar el pom de definición de dependencias para aprovechar la caché de capas
COPY pom-backend.xml ./pom-backend.xml

# Descargar las dependencias del backend en modo offline (optimización de caché)
RUN mvn -f pom-backend.xml dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar el proyecto saltándose los tests (los tests se ejecutan en CI/CD o manualmente)
RUN mvn -f pom-backend.xml clean package -DskipTests

# ==============================================================================
# Fase 2: Ejecución (JRE Alpine ligero + Seguridad No-Root)
# ==============================================================================
FROM eclipse-temurin:21.0.5_11-jre-alpine
WORKDIR /app

# Copiar el jar compilado desde la fase de construcción
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto de la API REST
EXPOSE 8080

# Usar usuario sin privilegios por seguridad (UID/GID 1000)
USER 1000:1000

# Comando para iniciar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
