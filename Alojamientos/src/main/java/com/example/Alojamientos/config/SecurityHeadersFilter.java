package com.example.Alojamientos.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.EnumSet;

/**
 * SecurityHeadersFilter — AUDIT-1
 *
 * Filtro global que añade cabeceras de seguridad HTTP a TODAS las respuestas
 * del servidor, incluyendo errores 404, 500 y cualquier ruta no cubierta por
 * el SecurityFilterChain de Spring Security.
 *
 * Se registra con dispatcherTypes REQUEST + ERROR para cubrir también
 * las respuestas de error generadas por Tomcat.
 *
 * Cabeceras añadidas:
 * - Content-Security-Policy : restringe las fuentes de contenido al propio dominio
 * - X-Frame-Options          : evita ataques de clickjacking
 * - X-Content-Type-Options   : evita MIME-type sniffing
 * - Referrer-Policy          : controla la información de referencia enviada
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Restringe las fuentes de contenido al propio dominio — mitiga XSS
        response.setHeader(
                "Content-Security-Policy",
                "default-src 'self'; " +
                        "script-src 'self'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self'; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'"
        );

        // Evita que la página sea embebida en iframes — mitiga clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // Evita que el navegador detecte el tipo MIME automáticamente
        response.setHeader("X-Content-Type-Options", "nosniff");

        // No enviar información de referencia en peticiones cross-origin
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        filterChain.doFilter(request, response);
    }

    /**
     * Sobrescribe para NO omitir el filtro en dispatches de tipo ERROR.
     * Por defecto OncePerRequestFilter omite los re-dispatches de error
     * de Tomcat — esto lo habilita explícitamente.
     */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    /**
     * Registro explícito del filtro con dispatcherTypes REQUEST + ERROR
     * para garantizar cobertura en respuestas de error 404/500 de Tomcat.
     */
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration() {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>(this);
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}