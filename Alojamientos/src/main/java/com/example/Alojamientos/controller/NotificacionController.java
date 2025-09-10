package com.example.Alojamientos.controller;

import com.example.Alojamientos.dto.NotificacionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Gestión de notificaciones del sistema")
public class NotificacionController {

    @GetMapping
    @Operation(summary = "Listar todas las notificaciones",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de notificaciones obtenida",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificacionDTO.class)),
                                    examples = @ExampleObject(value = "[{\"id\":1,\"idUsuario\":5,\"tipo\":\"NUEVA_RESERVA\",\"titulo\":\"Nueva reserva creada\",\"mensaje\":\"Tu reserva ha sido confirmada\",\"leida\":false,\"fechaCreacion\":\"2024-10-10T12:00:00\"}]"))),
                    @ApiResponse(responseCode = "204", description = "No hay notificaciones registradas"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<List<NotificacionDTO>> getAll() {
        return ResponseEntity.ok(Collections.emptyList()); // mock
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una notificación por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notificación encontrada",
                            content = @Content(schema = @Schema(implementation = NotificacionDTO.class),
                                    examples = @ExampleObject(value = "{\"id\":1,\"idUsuario\":5,\"tipo\":\"NUEVA_RESERVA\",\"titulo\":\"Nueva reserva creada\",\"mensaje\":\"Tu reserva ha sido confirmada\",\"leida\":false,\"fechaCreacion\":\"2024-10-10T12:00:00\"}"))),
                    @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<NotificacionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new NotificacionDTO()); // mock
    }

    @PostMapping
    @Operation(summary = "Crear una nueva notificación",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificacionDTO.class),
                            examples = @ExampleObject(value = "{\"idUsuario\":5,\"tipo\":\"NUEVO_COMENTARIO\",\"titulo\":\"Nuevo comentario\",\"mensaje\":\"Un huésped comentó tu alojamiento\",\"leida\":false}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Notificación creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            })
    public ResponseEntity<String> create(@RequestBody NotificacionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Notificación creada (mock)");
    }

    @PutMapping("/{id}/leer")
    @Operation(summary = "Marcar una notificación como leída",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notificación marcada como leída"),
                    @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno al actualizar notificación")
            })
    public ResponseEntity<String> marcarComoLeida(@PathVariable Long id) {
        return ResponseEntity.ok("Notificación marcada como leída (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una notificación",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Notificación eliminada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar notificación")
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
