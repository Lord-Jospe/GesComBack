# =========================
# Stage 1: Build
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar pom y descargar dependencias primero (mejor cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el resto del proyecto
COPY src ./src

# Compilar la app
RUN mvn clean package -DskipTests

# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copiar el jar generado
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto dinámico
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
