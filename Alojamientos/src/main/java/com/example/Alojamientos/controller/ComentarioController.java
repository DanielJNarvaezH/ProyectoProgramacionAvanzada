package com.example.Alojamientos.controller;

import com.example.Alojamientos.dto.ComentarioDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/comentarios")
@Tag(name = "Comentarios", description = "Gestión de comentarios en alojamientos")
public class ComentarioController {

    @GetMapping
    @Operation(summary = "Listar todos los comentarios",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de comentarios obtenida",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComentarioDTO.class)),
                                    examples = @ExampleObject(value = "[{\"id\":1,\"idAlojamiento\":5,\"idUsuario\":2,\"texto\":\"Muy buen lugar\",\"fecha\":\"2024-10-01\"}]"))),
                    @ApiResponse(responseCode = "204", description = "No hay comentarios registrados"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<List<ComentarioDTO>> getAll() {
        return ResponseEntity.ok(Collections.emptyList()); // mock
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un comentario por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Comentario encontrado",
                            content = @Content(schema = @Schema(implementation = ComentarioDTO.class),
                                    examples = @ExampleObject(value = "{\"id\":1,\"idAlojamiento\":5,\"idUsuario\":2,\"texto\":\"Muy buen lugar\",\"fecha\":\"2024-10-01\"}"))),
                    @ApiResponse(responseCode = "404", description = "Comentario no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<ComentarioDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ComentarioDTO()); // mock
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo comentario",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ComentarioDTO.class),
                            examples = @ExampleObject(value = "{\"idAlojamiento\":5,\"idUsuario\":2,\"texto\":\"Excelente atención\",\"fecha\":\"2024-10-15\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Comentario creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento o Usuario no encontrado")
            })
    public ResponseEntity<String> create(@RequestBody ComentarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Comentario creado (mock)");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un comentario existente",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"texto\":\"Se actualizó la opinión\",\"fecha\":\"2024-10-20\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Comentario actualizado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Comentario no encontrado")
            })
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody ComentarioDTO dto) {
        return ResponseEntity.ok("Comentario actualizado (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un comentario",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Comentario eliminado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Comentario no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar comentario")
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
