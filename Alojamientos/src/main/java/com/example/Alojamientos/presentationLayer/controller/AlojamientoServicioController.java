package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.AlojamientoServicioDTO;
import com.example.Alojamientos.businessLayer.service.AlojamientoServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alojamientos-servicios")
@Tag(name = "Alojamiento-Servicio", description = "Gestión de la relación entre alojamientos y servicios")
@RequiredArgsConstructor
public class AlojamientoServicioController {

    private final AlojamientoServicioService alojamientoServicioService;

    @PostMapping
    @Operation(
            summary = "Crear una nueva relación alojamiento-servicio",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AlojamientoServicioDTO.class),
                            examples = @ExampleObject(value = "{\"lodgingId\":10,\"serviceId\":3}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Relación creada exitosamente",
                            content = @Content(schema = @Schema(implementation = AlojamientoServicioDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos (relación ya existe u otros errores de validación)"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento o servicio no encontrado")
            }
    )
    public ResponseEntity<?> crearRelacion(@RequestBody AlojamientoServicioDTO dto) {
        try {
            AlojamientoServicioDTO creada = alojamientoServicioService.agregarServicioAAlojamiento(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada); // 201
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("no encontrado") || msg.contains("no existe") || msg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener una relación alojamiento-servicio por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Relación encontrada",
                            content = @Content(schema = @Schema(implementation = AlojamientoServicioDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Relación no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            }
    )
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            AlojamientoServicioDTO dto = alojamientoServicioService.obtenerPorId(id);
            return ResponseEntity.ok(dto); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la relación: " + e.getMessage()); // 500
        }
    }

    @GetMapping("/alojamiento/{alojamientoId}/servicios")
    @Operation(
            summary = "Listar servicios asociados a un alojamiento",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de servicios obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlojamientoServicioDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "ID de alojamiento inválido"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            }
    )
    public ResponseEntity<?> listarServiciosDeAlojamiento(@PathVariable Integer alojamientoId) {
        if (alojamientoId == null || alojamientoId <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID de alojamiento inválido"); // 400
        }

        try {
            List<AlojamientoServicioDTO> lista = alojamientoServicioService.listarServiciosDeAlojamiento(alojamientoId);
            return ResponseEntity.ok(lista); // 200 (lista puede estar vacía)
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar servicios del alojamiento: " + e.getMessage()); // 500
        }
    }

    @GetMapping("/servicio/{servicioId}/alojamientos")
    @Operation(
            summary = "Listar alojamientos que tienen un servicio específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de alojamientos obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlojamientoServicioDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "ID de servicio inválido"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            }
    )
    public ResponseEntity<?> listarAlojamientosPorServicio(@PathVariable Integer servicioId) {
        if (servicioId == null || servicioId <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID de servicio inválido"); // 400
        }

        try {
            List<AlojamientoServicioDTO> lista = alojamientoServicioService.listarAlojamientosPorServicio(servicioId);
            return ResponseEntity.ok(lista); // 200
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar alojamientos por servicio: " + e.getMessage()); // 500
        }
    }

    @DeleteMapping("/alojamiento/{alojamientoId}/servicio/{servicioId}")
    @Operation(
            summary = "Eliminar una relación alojamiento-servicio por alojamiento y servicio",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Relación eliminada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Relación no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar relación")
            }
    )
    public ResponseEntity<?> eliminarRelacionPorAlojamientoYServicio(
            @PathVariable Integer alojamientoId,
            @PathVariable Integer servicioId) {
        try {
            alojamientoServicioService.eliminarServicioDeAlojamiento(alojamientoId, servicioId);
            return ResponseEntity.noContent().build(); // 204
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la relación: " + e.getMessage()); // 500
        }
    }

    @PutMapping("/{id}/desactivar")
    @Operation(
            summary = "Desactivar (soft delete) una relación alojamiento-servicio por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Relación desactivada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Relación no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno al desactivar relación")
            }
    )
    public ResponseEntity<?> desactivarRelacion(@PathVariable Integer id) {
        try {
            alojamientoServicioService.desactivarServicioDeAlojamiento(id);
            return ResponseEntity.ok("Relación desactivada correctamente"); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al desactivar la relación: " + e.getMessage()); // 500
        }
    }

    @GetMapping("/exists")
    @Operation(
            summary = "Verificar si un alojamiento tiene un servicio (boolean)",
            description = "Query params: alojamientoId, servicioId",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultado de existencia obtenido correctamente"),
                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            }
    )
    public ResponseEntity<?> alojamientoTieneServicio(
            @RequestParam(required = true) Integer alojamientoId,
            @RequestParam(required = true) Integer servicioId) {

        if (alojamientoId == null || alojamientoId <= 0 || servicioId == null || servicioId <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Parámetros 'alojamientoId' y 'servicioId' inválidos"); // 400
        }

        try {
            boolean tiene = alojamientoServicioService.alojamientoTieneServicio(alojamientoId, servicioId);
            return ResponseEntity.ok(tiene); // 200 -> body: true/false
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al verificar relación alojamiento-servicio: " + e.getMessage()); // 500
        }
    }
}

