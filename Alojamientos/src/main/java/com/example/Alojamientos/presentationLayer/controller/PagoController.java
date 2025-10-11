package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.PagoDTO;
import com.example.Alojamientos.businessLayer.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Gestión de pagos asociados a reservas")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    // ============================================================
    // Listar todos los pagos
    // ============================================================
    @GetMapping
    @Operation(summary = "Listar todos los pagos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de pagos obtenida",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagoDTO.class)))),
                    @ApiResponse(responseCode = "204", description = "No hay pagos registrados"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<?> getAll() {
        try {
            List<PagoDTO> pagos = pagoService.listarTodos();
            if (pagos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la lista de pagos");
        }
    }

    // ============================================================
    // Obtener pago por ID
    // ============================================================
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pago por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pago encontrado",
                            content = @Content(schema = @Schema(implementation = PagoDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
                    @ApiResponse(responseCode = "400", description = "ID inválido")
            })
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID inválido");
        }
        try {
            PagoDTO pago = pagoService.obtenerPorId(id);
            return ResponseEntity.ok(pago);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ============================================================
    // Registrar un nuevo pago
    // ============================================================
    @PostMapping
    @Operation(summary = "Registrar un nuevo pago",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagoDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Pago registrado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "409", description = "Pago ya registrado para la reserva")
            })
    public ResponseEntity<?> create(@Valid @RequestBody PagoDTO dto) {
        try {
            PagoDTO nuevoPago = pagoService.registrarPago(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPago);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Ya existe un pago")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ============================================================
    // Confirmar pago completado
    // ============================================================
    @PutMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar pago completado",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pago confirmado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Referencia externa inválida")
            })
    public ResponseEntity<?> confirmarPago(@PathVariable Integer id, @RequestParam String externalRef) {
        if (externalRef == null || externalRef.isBlank()) {
            return ResponseEntity.badRequest().body("Referencia externa inválida");
        }
        try {
            PagoDTO pago = pagoService.confirmarPago(id, externalRef);
            return ResponseEntity.ok(pago);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ============================================================
    // Marcar pago como fallido
    // ============================================================
    @PutMapping("/{id}/fallido")
    @Operation(summary = "Marcar pago como fallido",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pago marcado como fallido"),
                    @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
                    @ApiResponse(responseCode = "400", description = "ID inválido")
            })
    public ResponseEntity<?> marcarComoFallido(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID inválido");
        }
        try {
            PagoDTO pago = pagoService.marcarComoFallido(id);
            return ResponseEntity.ok(pago);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ============================================================
    // Obtener pago por reserva
    // ============================================================
    @GetMapping("/reserva/{reservaId}")
    @Operation(summary = "Obtener pago por reserva",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pago encontrado"),
                    @ApiResponse(responseCode = "404", description = "Pago no encontrado para la reserva"),
                    @ApiResponse(responseCode = "400", description = "ID de reserva inválido")
            })
    public ResponseEntity<?> obtenerPorReserva(@PathVariable Integer reservaId) {
        if (reservaId == null || reservaId <= 0) {
            return ResponseEntity.badRequest().body("ID de reserva inválido");
        }
        try {
            PagoDTO pago = pagoService.obtenerPorReserva(reservaId);
            return ResponseEntity.ok(pago);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

