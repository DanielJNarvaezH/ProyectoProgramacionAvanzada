package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.ReservaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Gestión de reservas de alojamientos")
public class ReservaController {

    @GetMapping
    @Operation(summary = "Listar todas las reservas",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de reservas obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReservaDTO.class)),
                                    examples = @ExampleObject(value = "[{\"id\":1,\"idHuesped\":2,\"idAlojamiento\":5,\"fechaInicio\":\"2025-10-01\",\"fechaFin\":\"2025-10-05\",\"precioTotal\":800.0}]"))),
                    @ApiResponse(responseCode = "204", description = "No hay reservas registradas"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<List<ReservaDTO>> getAll() {
        return ResponseEntity.ok(Collections.emptyList()); // mock
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una reserva por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reserva encontrada",
                            content = @Content(schema = @Schema(implementation = ReservaDTO.class),
                                    examples = @ExampleObject(value = "{\"id\":1,\"idHuesped\":2,\"idAlojamiento\":5,\"fechaInicio\":\"2025-10-01\",\"fechaFin\":\"2025-10-05\",\"precioTotal\":800.0}"))),
                    @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<ReservaDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ReservaDTO()); // mock
    }

    @PostMapping
    @Operation(summary = "Crear una nueva reserva",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservaDTO.class),
                            examples = @ExampleObject(value = "{\"idHuesped\":2,\"idAlojamiento\":5,\"fechaInicio\":\"2025-10-01\",\"fechaFin\":\"2025-10-05\",\"numHuespedes\":3}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos (fechas o número de huéspedes incorrectos)"),
                    @ApiResponse(responseCode = "409", description = "Conflicto de fechas con otra reserva existente")
            })
    public ResponseEntity<String> create(@RequestBody ReservaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Reserva creada (mock)");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una reserva existente",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"fechaInicio\":\"2025-10-02\",\"fechaFin\":\"2025-10-06\",\"numHuespedes\":4}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reserva actualizada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
            })
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody ReservaDTO dto) {
        return ResponseEntity.ok("Reserva actualizada (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar una reserva",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Reserva cancelada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno al cancelar reserva")
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
