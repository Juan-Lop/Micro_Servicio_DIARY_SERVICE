# Etapa 1: Construcción
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Descargar dependencias (cache layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN ./mvnw clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre-alpine

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR construido desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Cambiar a usuario no-root
USER spring:spring

# Exponer puerto de la aplicación
EXPOSE 8082

# Variables de entorno por defecto (serán sobrescritas por Render)
ENV JAVA_OPTS="-Xmx512m -Xms256m" \
    SERVER_PORT=8082

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
