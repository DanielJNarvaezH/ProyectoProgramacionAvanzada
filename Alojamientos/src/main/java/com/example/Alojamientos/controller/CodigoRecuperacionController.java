package com.example.Alojamientos.controller;

import com.example.Alojamientos.dto.CodigoRecuperacionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/codigos-recuperacion")
@Tag(name = "Códigos de Recuperación", description = "Gestión de códigos de recuperación de contraseña")
public class CodigoRecuperacionController {

    @GetMapping
    @Operation(summary = "Listar todos los códigos de recuperación",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de códigos obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CodigoRecuperacionDTO.class)),
                                    examples = @ExampleObject(value = "[{\"id\":1,\"idUsuario\":5,\"codigo\":\"123456\",\"usado\":false}]"))),
                    @ApiResponse(responseCode = "204", description = "No hay códigos registrados"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<List<CodigoRecuperacionDTO>> getAll() {
        return ResponseEntity.ok(Collections.emptyList()); // mock
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un código por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Código encontrado",
                            content = @Content(schema = @Schema(implementation = CodigoRecuperacionDTO.class),
                                    examples = @ExampleObject(value = "{\"id\":1,\"idUsuario\":5,\"codigo\":\"123456\",\"usado\":false}"))),
                    @ApiResponse(responseCode = "404", description = "Código no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<CodigoRecuperacionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new CodigoRecuperacionDTO()); // mock
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo código de recuperación",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CodigoRecuperacionDTO.class),
                            examples = @ExampleObject(value = "{\"idUsuario\":5,\"codigo\":\"654321\",\"fechaExpiracion\":\"2025-09-09T10:00:00\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Código creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "409", description = "Ya existe un código activo para este usuario")
            })
    public ResponseEntity<String> create(@RequestBody CodigoRecuperacionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Código creado (mock)");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un código de recuperación",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"usado\":true}"))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Código actualizado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Código no encontrado")
            })
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody CodigoRecuperacionDTO dto) {
        return ResponseEntity.ok("Código actualizado (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un código de recuperación",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Código eliminado"),
                    @ApiResponse(responseCode = "404", description = "Código no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar código")
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
