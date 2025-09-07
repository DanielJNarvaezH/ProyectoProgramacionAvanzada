package com.example.Alojamientos.controller;

import com.example.Alojamientos.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Crear y gestionar reservas")
public class ReservaController {

    @PostMapping
    @Operation(summary = "Crear reserva (usuario)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = ReservaCreateDTO.class),
                    examples = @ExampleObject(value = "{\"accommodationId\":1,\"checkIn\":\"2025-12-01\",\"checkOut\":\"2025-12-05\",\"guests\":2}"))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Reserva confirmada, emails enviados"),
                    @ApiResponse(responseCode = "400", description = "Validaciones")
            }
    )
    public ResponseEntity<String> create(@RequestBody ReservaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Reserva confirmada (mock)");
    }

    @GetMapping
    @Operation(summary = "Listar reservas (user o admin), filtros por estado/fechas")
    public ResponseEntity<List<ReservaCreateDTO>> list(
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reserva por id")
    public ResponseEntity<ReservaCreateDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(new ReservaCreateDTO());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cancelar reserva (usuario) o cambiar estado (anfitrión)")
    public ResponseEntity<String> updateState(@PathVariable Long id, @RequestBody Map<String,String> body) {
        return ResponseEntity.ok("Estado actualizado (mock)");
    }
}

