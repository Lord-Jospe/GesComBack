# Usa imagen oficial de Java
FROM eclipse-temurin:17-jdk

# Directorio de trabajo
WORKDIR /app

# Copiar proyecto
COPY . .

# Construir proyecto
RUN ./mvnw clean package -DskipTests

# Exponer puerto
EXPOSE 8080

# Ejecutar aplicación
CMD ["java", "-jar", "target/GesCom-0.0.1-SNAPSHOT.jar"]
