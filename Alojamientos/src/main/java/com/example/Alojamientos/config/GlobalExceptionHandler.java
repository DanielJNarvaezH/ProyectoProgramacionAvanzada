package com.example.Alojamientos.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — AUDIT-2
 *
 * Intercepta todas las excepciones no controladas y retorna
 * respuestas JSON genéricas sin exponer stack traces,
 * versión de Tomcat ni información interna del servidor.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ─────────────────────────────────────────────
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            NoHandlerFoundException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND,
                "El recurso solicitado no existe.",
                request.getRequestURI());
    }

    // ── 400 Validación ────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Datos de entrada inválidos.");
        return buildError(HttpStatus.BAD_REQUEST, detalle, request.getRequestURI());
    }

    // ── 400 Argumento ilegal ──────────────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST,
                ex.getMessage() != null ? ex.getMessage() : "Solicitud inválida.",
                request.getRequestURI());
    }

    // ── 500 Cualquier otra excepción ──────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex,
            HttpServletRequest request) {
        // NO exponer ex.getMessage() ni stack trace en producción
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor. Por favor intenta más tarde.",
                request.getRequestURI());
    }

    // ── Builder de respuesta genérica ─────────────────────────────
    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status,
            String mensaje,
            String ruta) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status.value());
        body.put("error",     status.getReasonPhrase());
        body.put("mensaje",   mensaje);
        body.put("ruta",      ruta);
        return ResponseEntity.status(status).body(body);
    }
}
