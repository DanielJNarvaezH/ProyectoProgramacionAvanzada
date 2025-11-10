# Etapa 1: Construcción
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle build -x test || return 0
COPY src ./src
RUN gradle clean bootJar -x test

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Variables de entorno para conectar a la base de datos (contenedor llamado "db")
ENV SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/alojamientos_db
ENV SPRING_DATASOURCE_USERNAME=alojamientos_user
ENV SPRING_DATASOURCE_PASSWORD=familia727230228
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=update

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
