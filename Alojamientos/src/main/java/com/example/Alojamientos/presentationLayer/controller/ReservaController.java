package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.ReservaDTO;
import com.example.Alojamientos.businessLayer.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Gestión de reservas de alojamientos")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    // ============================================================
// Listar reservas de un huésped
// ============================================================
    @GetMapping("/huesped/{guestId}")
    @Operation(summary = "Listar reservas de un huésped",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reservas obtenidas correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReservaDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "ID de huésped inválido"),
                    @ApiResponse(responseCode = "404", description = "No se encontraron reservas para el huésped")
            })
    public ResponseEntity<?> listarPorHuesped(@PathVariable Integer guestId) {
        if (guestId == null || guestId <= 0) {
            return ResponseEntity.badRequest().body("ID de huésped inválido"); // 400
        }
        List<ReservaDTO> lista = reservaService.listarPorHuesped(guestId);
        if (lista.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontraron reservas para el huésped"); // 404
        }
        return ResponseEntity.ok(lista); // 200
    }

    // ============================================================
// Listar reservas de un alojamiento
// ============================================================
    @GetMapping("/alojamiento/{lodgingId}")
    @Operation(summary = "Listar reservas de un alojamiento",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reservas obtenidas correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReservaDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "ID de alojamiento inválido"),
                    @ApiResponse(responseCode = "404", description = "No se encontraron reservas para el alojamiento")
            })
    public ResponseEntity<?> listarPorAlojamiento(@PathVariable Integer lodgingId) {
        if (lodgingId == null || lodgingId <= 0) {
            return ResponseEntity.badRequest().body("ID de alojamiento inválido"); // 400
        }
        List<ReservaDTO> lista = reservaService.listarPorAlojamiento(lodgingId);
        if (lista.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontraron reservas para el alojamiento"); // 404
        }
        return ResponseEntity.ok(lista); // 200
    }


    // ============================================================
    // Obtener reserva por ID
    // ============================================================
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una reserva por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reserva encontrada",
                            content = @Content(schema = @Schema(implementation = ReservaDTO.class))),
                    @ApiResponse(responseCode = "400", description = "ID inválido"),
                    @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
            })
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("ID inválido"); // 400
            }
            ReservaDTO dto = reservaService.obtenerPorId(id);
            return ResponseEntity.ok(dto); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    // ============================================================
    // Crear nueva reserva
    // ============================================================
    @PostMapping
    @Operation(summary = "Crear una nueva reserva",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservaDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "409", description = "Conflicto de fechas o capacidad excedida")
            })
    public ResponseEntity<?> create(@Valid @RequestBody ReservaDTO dto) {
        try {
            ReservaDTO nueva = reservaService.crearReserva(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva); // 201
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("no disponible") || msg.contains("excede la capacidad") || msg.contains("solapamiento")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409
            }
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }

    // ============================================================
    // Cancelar reserva
    // ============================================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar una reserva",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reserva cancelada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Cancelación no permitida o datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
            })
    public ResponseEntity<?> cancelar(@PathVariable Integer id, @RequestParam String motivo) {
        try {
            reservaService.cancelarReserva(id, motivo);
            return ResponseEntity.ok("Reserva cancelada correctamente"); // 200
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("no se puede cancelar") || msg.contains("motivo") || msg.contains("48 horas") || msg.contains("ya fue cancelada")) {
                return ResponseEntity.badRequest().body(e.getMessage()); // 400
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    // ============================================================
    // Marcar reserva como completada
    // ============================================================
    @PutMapping("/{id}/completar")
    @Operation(summary = "Marcar una reserva como completada",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reserva marcada como completada"),
                    @ApiResponse(responseCode = "400", description = "No se puede completar la reserva aún"),
                    @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
            })
    public ResponseEntity<?> completar(@PathVariable Integer id) {
        try {
            reservaService.marcarComoCompletada(id);
            return ResponseEntity.ok("Reserva marcada como completada"); // 200
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("aún no ha finalizado")) {
                return ResponseEntity.badRequest().body(e.getMessage()); // 400
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }
}
