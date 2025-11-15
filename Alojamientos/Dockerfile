# Etapa 1: Build
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app

# Copiar todos los archivos necesarios para un build Gradle
COPY build.gradle settings.gradle ./
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY src ./src

# Dar permisos al wrapper
RUN chmod +x gradlew

# Construir JAR
RUN ./gradlew clean bootJar -x test

# Etapa 2: Runtime
FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
