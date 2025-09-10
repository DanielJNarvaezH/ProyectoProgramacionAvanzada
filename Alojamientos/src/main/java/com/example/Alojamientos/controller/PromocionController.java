package com.example.Alojamientos.controller;

import com.example.Alojamientos.dto.PromocionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/promociones")
@Tag(name = "Promociones", description = "Gestión de promociones para los alojamientos")
public class PromocionController {

    @GetMapping
    @Operation(summary = "Listar todas las promociones",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de promociones obtenida",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PromocionDTO.class)),
                                    examples = @ExampleObject(value = "[{\"id\":1,\"descripcion\":\"Descuento 15%\",\"fechaInicio\":\"2025-03-01\",\"fechaFin\":\"2025-03-15\",\"idAlojamiento\":7}]"))),
                    @ApiResponse(responseCode = "204", description = "No hay promociones registradas"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<List<PromocionDTO>> getAll() {
        return ResponseEntity.ok(Collections.emptyList()); // mock
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una promoción por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promoción encontrada",
                            content = @Content(schema = @Schema(implementation = PromocionDTO.class),
                                    examples = @ExampleObject(value = "{\"id\":1,\"descripcion\":\"Descuento 15%\",\"fechaInicio\":\"2025-03-01\",\"fechaFin\":\"2025-03-15\",\"idAlojamiento\":7}"))),
                    @ApiResponse(responseCode = "404", description = "Promoción no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<PromocionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new PromocionDTO()); // mock
    }

    @PostMapping
    @Operation(summary = "Crear una nueva promoción",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PromocionDTO.class),
                            examples = @ExampleObject(value = "{\"descripcion\":\"Descuento 15%\",\"fechaInicio\":\"2025-03-01\",\"fechaFin\":\"2025-03-15\",\"idAlojamiento\":7}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Promoción creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado")
            })
    public ResponseEntity<String> create(@RequestBody PromocionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Promoción creada (mock)");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una promoción existente",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PromocionDTO.class),
                            examples = @ExampleObject(value = "{\"descripcion\":\"Descuento 20%\",\"fechaInicio\":\"2025-03-05\",\"fechaFin\":\"2025-03-20\",\"idAlojamiento\":7}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promoción actualizada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Promoción no encontrada")
            })
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody PromocionDTO dto) {
        return ResponseEntity.ok("Promoción actualizada (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una promoción",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Promoción eliminada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Promoción no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar promoción")
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
