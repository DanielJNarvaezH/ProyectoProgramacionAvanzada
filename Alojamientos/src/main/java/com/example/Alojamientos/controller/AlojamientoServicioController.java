package com.example.Alojamientos.controller;

import com.example.Alojamientos.dto.AlojamientoServicioDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/alojamientos-servicios")
@Tag(name = "Alojamiento-Servicio", description = "Gestión de la relación entre alojamientos y servicios")
public class AlojamientoServicioController {

    @GetMapping
    @Operation(summary = "Listar todas las relaciones alojamiento-servicio",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de relaciones obtenida",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlojamientoServicioDTO.class)),
                                    examples = @ExampleObject(value = "[{\"id\":1,\"idAlojamiento\":10,\"idServicio\":3}]"))),
                    @ApiResponse(responseCode = "204", description = "No existen relaciones registradas"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<List<AlojamientoServicioDTO>> getAll() {
        return ResponseEntity.ok(Collections.emptyList()); // mock
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una relación alojamiento-servicio por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Relación encontrada",
                            content = @Content(schema = @Schema(implementation = AlojamientoServicioDTO.class),
                                    examples = @ExampleObject(value = "{\"id\":1,\"idAlojamiento\":10,\"idServicio\":3}"))),
                    @ApiResponse(responseCode = "404", description = "Relación no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<AlojamientoServicioDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new AlojamientoServicioDTO()); // mock
    }

    @PostMapping
    @Operation(summary = "Crear una nueva relación alojamiento-servicio",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AlojamientoServicioDTO.class),
                            examples = @ExampleObject(value = "{\"idAlojamiento\":10,\"idServicio\":3}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Relación creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento o servicio no encontrado")
            })
    public ResponseEntity<String> create(@RequestBody AlojamientoServicioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Relación creada (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una relación alojamiento-servicio",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Relación eliminada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Relación no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar relación")
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
