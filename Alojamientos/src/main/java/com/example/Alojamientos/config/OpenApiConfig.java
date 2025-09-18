package com.example.Alojamientos.config;

// ====================== IMPORTACIONES NECESARIAS ======================
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración de OpenAPI y Swagger para la API de Alojamientos.
 * Aquí definimos la información general de la API, los servidores disponibles
 * y el esquema de seguridad (JWT con Bearer Token).
 */
@Configuration  // Indica que esta clase es de configuración de Spring
@OpenAPIDefinition(
        info = @Info(
                title = "API - Plataforma de Alojamientos (Hosped)", // Nombre de la API en Swagger
                version = "1.0.0", // Versión de la API
                description = "API para gestión de usuarios, anfitriones, alojamientos, reservas y comentarios", // Descripción
                contact = @Contact( // Información de contacto del equipo de desarrollo
                        name = "Equipo Proyecto",
                        email = "dev@uq.edu.co",
                        url = "https://uq.edu.co"
                ),
                license = @License( // Licencia del software
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = { // Definición de los servidores (según perfiles: dev, test, prod)
                @Server(
                        url = "http://localhost:8080",
                        description = "Servidor de Desarrollo"
                ),
                @Server(
                        url = "http://localhost:8090",
                        description = "Servidor de Pruebas"
                ),
                @Server(
                        url = "http://localhost:8099",
                        description = "Servidor de Producción"
                )
        }
)
public class OpenApiConfig {

    /**
     * Bean que personaliza la configuración de OpenAPI.
     * Aquí agregamos el esquema de seguridad para JWT (Bearer Authentication)
     * y lo aplicamos de forma global a toda la API.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", // Nombre del esquema de seguridad
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP) // Tipo de autenticación HTTP
                                        .scheme("bearer") // Esquema Bearer
                                        .bearerFormat("JWT") // Formato del token
                                        .description("Ingresa tu token JWT con el formato: Bearer {token}") // Ayuda para el usuario
                        )
                )
                // Aplicamos el esquema de seguridad de forma GLOBAL
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
}

