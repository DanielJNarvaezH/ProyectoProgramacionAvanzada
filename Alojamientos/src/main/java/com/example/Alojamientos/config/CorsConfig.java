package com.example.Alojamientos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración CORS para la plataforma Hosped.
 *
 * <p>Permite que el frontend Angular (u otros clientes) pueda comunicarse
 * con la API REST incluyendo los headers de autenticación JWT necesarios.
 *
 * <p>Cambios respecto a la versión anterior (AUTH-10):
 * <ul>
 *   <li>Mapping corregido de "/alojamientos/api/**" a "/api/**" para evitar
 *       duplicación con el context-path ya definido en application.properties.</li>
 *   <li>Header "Authorization" expuesto explícitamente para permitir el envío
 *       del token JWT desde el frontend.</li>
 *   <li>Header "Authorization" añadido a exposedHeaders para que el frontend
 *       pueda leerlo en las respuestas cuando sea necesario.</li>
 * </ul>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders(
                        "Authorization",        // Token JWT → Bearer <token>
                        "Content-Type",         // application/json
                        "Accept",               // Tipo de respuesta esperado
                        "Origin",               // Origen de la request
                        "X-Requested-With"      // Identificador de requests AJAX
                )
                .exposedHeaders(
                        "Authorization"         // Permite al frontend leer el token en respuestas
                )
                .allowCredentials(true)
                .maxAge(3600);                  // Cache preflight OPTIONS por 1 hora (segundos)
    }
}