package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.PagoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Gestión de pagos asociados a reservas")
public class PagoController {

    @GetMapping
    @Operation(summary = "Listar todos los pagos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de pagos obtenida",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagoDTO.class)),
                                    examples = @ExampleObject(value = "[{\"id\":1,\"idReserva\":10,\"monto\":200.50,\"metodo\":\"TARJETA_CREDITO\",\"estado\":\"COMPLETADO\",\"referenciaExterna\":\"PAY12345\",\"fechaPago\":\"2025-03-05T14:30:00\"}]"))),
                    @ApiResponse(responseCode = "204", description = "No hay pagos registrados"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<List<PagoDTO>> getAll() {
        return ResponseEntity.ok(Collections.emptyList()); // mock
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pago por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pago encontrado",
                            content = @Content(schema = @Schema(implementation = PagoDTO.class),
                                    examples = @ExampleObject(value = "{\"id\":1,\"idReserva\":10,\"monto\":200.50,\"metodo\":\"TARJETA_CREDITO\",\"estado\":\"COMPLETADO\",\"referenciaExterna\":\"PAY12345\",\"fechaPago\":\"2025-03-05T14:30:00\"}"))),
                    @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<PagoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new PagoDTO()); // mock
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo pago",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagoDTO.class),
                            examples = @ExampleObject(value = "{\"idReserva\":10,\"monto\":200.50,\"metodo\":\"TARJETA_CREDITO\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Pago registrado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
            })
    public ResponseEntity<String> create(@RequestBody PagoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Pago registrado (mock)");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un pago existente",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"estado\":\"REEMBOLSADO\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pago actualizado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Pago no encontrado")
            })
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody PagoDTO dto) {
        return ResponseEntity.ok("Pago actualizado (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un pago",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Pago eliminado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar el pago")
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
