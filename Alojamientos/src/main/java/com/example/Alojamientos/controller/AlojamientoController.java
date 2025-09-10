package com.example.Alojamientos.controller;

import com.example.Alojamientos.dto.AlojamientoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/alojamientos")
@Tag(name = "Alojamientos", description = "Gestión de alojamientos (casas, apartamentos, habitaciones, etc.)")
public class AlojamientoController {

    @GetMapping
    @Operation(summary = "Listar todos los alojamientos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de alojamientos obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlojamientoDTO.class)),
                                    examples = @ExampleObject(value = "[{\"id\":1,\"nombre\":\"Casa en la playa\",\"direccion\":\"Calle 123, Cartagena\",\"precio\":200.0}]"))),
                    @ApiResponse(responseCode = "204", description = "No hay alojamientos registrados"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<List<AlojamientoDTO>> getAll() {
        return ResponseEntity.ok(Collections.emptyList()); // mock
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un alojamiento por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Alojamiento encontrado",
                            content = @Content(schema = @Schema(implementation = AlojamientoDTO.class),
                                    examples = @ExampleObject(value = "{\"id\":1,\"nombre\":\"Casa en la playa\",\"direccion\":\"Calle 123, Cartagena\",\"precio\":200.0}"))),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<AlojamientoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new AlojamientoDTO()); // mock
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo alojamiento",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AlojamientoDTO.class),
                            examples = @ExampleObject(value = "{\"nombre\":\"Apartamento moderno\",\"direccion\":\"Carrera 45, Medellín\",\"precio\":150.0}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Alojamiento creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "409", description = "Ya existe un alojamiento con esos datos")
            })
    public ResponseEntity<String> create(@RequestBody AlojamientoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Alojamiento creado (mock)");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un alojamiento",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"nombre\":\"Casa remodelada\",\"direccion\":\"Avenida Siempre Viva 742\",\"precio\":180.0}"))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Alojamiento actualizado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado")
            })
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody AlojamientoDTO dto) {
        return ResponseEntity.ok("Alojamiento actualizado (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un alojamiento",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Alojamiento eliminado"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar alojamiento")
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
