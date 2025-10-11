package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.NotificacionDTO;
import com.example.Alojamientos.businessLayer.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Gestión de notificaciones del sistema")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    // ============================================================
    // Crear notificación
    // ============================================================
    @PostMapping
    @Operation(summary = "Crear una nueva notificación para un usuario",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificacionDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Notificación creada exitosamente",
                            content = @Content(schema = @Schema(implementation = NotificacionDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos o tipo de notificación no soportado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            })
    public ResponseEntity<?> crearNotificacion(@Valid @RequestBody NotificacionDTO dto) {
        try {
            NotificacionDTO creada = notificacionService.crearNotificacion(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            }
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }

    // ============================================================
    // Listar notificaciones de un usuario (todas)
    // ============================================================
    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar notificaciones de un usuario (no leídas primero)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de notificaciones obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificacionDTO.class)))),
                    @ApiResponse(responseCode = "204", description = "El usuario no tiene notificaciones"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            })
    public ResponseEntity<?> listarPorUsuario(@PathVariable Integer usuarioId) {
        try {
            List<NotificacionDTO> lista = notificacionService.listarPorUsuario(usuarioId);
            if (lista.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204
            }
            return ResponseEntity.ok(lista); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    // ============================================================
    // Listar solo notificaciones no leídas
    // ============================================================
    @GetMapping("/usuario/{usuarioId}/no-leidas")
    @Operation(summary = "Listar solo notificaciones no leídas de un usuario",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de notificaciones no leídas obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificacionDTO.class)))),
                    @ApiResponse(responseCode = "204", description = "No hay notificaciones no leídas para el usuario"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            })
    public ResponseEntity<?> listarNoLeidas(@PathVariable Integer usuarioId) {
        try {
            List<NotificacionDTO> lista = notificacionService.listarNoLeidas(usuarioId);
            if (lista.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204
            }
            return ResponseEntity.ok(lista); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    // ============================================================
    // Obtener notificación por ID
    // ============================================================
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una notificación específica por su ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notificación encontrada correctamente",
                            content = @Content(schema = @Schema(implementation = NotificacionDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
                    @ApiResponse(responseCode = "400", description = "ID inválido o mal formado")
            })
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            NotificacionDTO dto = notificacionService.obtenerPorId(id);
            return ResponseEntity.ok(dto); // 200
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            }
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }

    // ============================================================
    // Contar notificaciones no leídas
    // ============================================================
    @GetMapping("/usuario/{usuarioId}/contar-no-leidas")
    @Operation(summary = "Contar notificaciones no leídas de un usuario",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cantidad de notificaciones no leídas obtenida correctamente",
                            content = @Content(schema = @Schema(implementation = Long.class))),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "ID inválido")
            })
    public ResponseEntity<?> contarNoLeidas(@PathVariable Integer usuarioId) {
        // Escenario 400: ID inválido
        if (usuarioId == null || usuarioId <= 0) {
            return ResponseEntity.badRequest().body("El ID del usuario es inválido");
        }

        try {
            Long cantidad = notificacionService.contarNoLeidas(usuarioId);
            // Escenario 200: Éxito
            return ResponseEntity.ok(Map.of("noLeidas", cantidad));
        } catch (IllegalArgumentException e) {
            // Escenario 404: Usuario no encontrado
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    // ============================================================
    // Marcar una notificación como leída
    // ============================================================
    @PutMapping("/{id}/leer")
    @Operation(summary = "Marcar una notificación específica como leída",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notificación marcada como leída correctamente",
                            content = @Content(schema = @Schema(implementation = NotificacionDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
                    @ApiResponse(responseCode = "400", description = "Error al procesar la solicitud")
            })
    public ResponseEntity<?> marcarComoLeida(@PathVariable Integer id) {
        try {
            NotificacionDTO actualizada = notificacionService.marcarComoLeida(id);
            return ResponseEntity.ok(actualizada); // 200
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            }
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }

    // ============================================================
    // Marcar todas las notificaciones de un usuario como leídas
    // ============================================================
    @PutMapping("/usuario/{usuarioId}/leer-todas")
    @Operation(summary = "Marcar todas las notificaciones de un usuario como leídas",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Todas las notificaciones marcadas como leídas"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Error de solicitud")
            })
    public ResponseEntity<?> marcarTodasComoLeidas(@PathVariable Integer usuarioId) {
        try {
            notificacionService.marcarTodasComoLeidas(usuarioId);
            return ResponseEntity.ok("Todas las notificaciones marcadas como leídas"); // 200
        } catch (IllegalArgumentException e) {
            if (e.getMessage().toLowerCase().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            }
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }

    // ============================================================
    // Eliminar una notificación
    // ============================================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una notificación específica por su ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Notificación eliminada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
                    @ApiResponse(responseCode = "400", description = "Error de solicitud o ID inválido")
            })
    public ResponseEntity<?> eliminarNotificacion(@PathVariable Integer id) {
        try {
            notificacionService.eliminarNotificacion(id);
            return ResponseEntity.noContent().build(); // 204
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            }
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }

    // ============================================================
    // Eliminar todas las notificaciones leídas de un usuario
    // ============================================================
    @DeleteMapping("/usuario/{usuarioId}/leidas")
    @Operation(summary = "Eliminar todas las notificaciones leídas de un usuario",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notificaciones leídas eliminadas correctamente"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Error de solicitud o usuario inválido")
            })
    public ResponseEntity<?> eliminarLeidasDeUsuario(@PathVariable Integer usuarioId) {
        try {
            notificacionService.eliminarLeidasDeUsuario(usuarioId);
            return ResponseEntity.ok("Notificaciones leídas eliminadas correctamente"); // 200
        } catch (IllegalArgumentException e) {
            if (e.getMessage().toLowerCase().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            }
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }
}

