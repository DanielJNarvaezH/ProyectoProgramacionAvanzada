package com.example.Alojamientos.controller;

import org.springframework.web.bind.annotation.RequestBody;
import com.example.Alojamientos.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/alojamientos")
@Tag(name = "Alojamientos", description = "Búsqueda y gestión de alojamientos")
public class AlojamientoController {

    @GetMapping
    @Operation(summary = "Buscar/listar alojamientos (paginado + filtros)")
    public ResponseEntity<List<AlojamientoCreateDTO>> search(
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "checkIn", required = false) String checkIn,
            @RequestParam(value = "checkOut", required = false) String checkOut,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "services", required = false) List<String> services,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        // Mock response - retornar lista vacía
        return ResponseEntity.ok(Collections.emptyList());
    }

    @PostMapping
    @Operation(summary = "Crear alojamiento (anfitrión)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = AlojamientoCreateDTO.class),
                    examples = @ExampleObject(value = "{\"title\":\"Casa en el campo\",\"description\":\"Bonita finca\",\"city\":\"Armenia\",\"address\":\"Via X\",\"latitude\":4.5319,\"longitude\":-75.6500,\"pricePerNight\":120.0,\"capacity\":6,\"services\":[\"wifi\",\"piscina\"],\"images\":[\"url1\",\"url2\"]}"))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Alojamiento creado"),
                    @ApiResponse(responseCode = "400", description = "Validación"),
                    @ApiResponse(responseCode = "403", description = "No es anfitrión")
            }
    )
    public ResponseEntity<String> create(@RequestBody AlojamientoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Alojamiento creado (mock)");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de alojamiento")
    public ResponseEntity<AlojamientoCreateDTO> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(new AlojamientoCreateDTO());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar alojamiento (propietario)")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody AlojamientoCreateDTO dto) {
        return ResponseEntity.ok("Actualizado (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete alojamiento (solo si no hay reservas futuras)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Consultar disponibilidad por rango de fechas")
    public ResponseEntity<Map<String, Boolean>> availability(
            @PathVariable Long id,
            @RequestParam(value = "checkIn") String checkIn,
            @RequestParam(value = "checkOut") String checkOut
    ) {
        Map<String, Boolean> resp = Map.of("available", true);
        return ResponseEntity.ok(resp);
    }
}
