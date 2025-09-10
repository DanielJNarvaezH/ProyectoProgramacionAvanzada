package com.example.Alojamientos.controller;

import com.example.Alojamientos.dto.RespuestaComentarioDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/respuestas-comentarios")
@Tag(name = "Respuestas a Comentarios", description = "Gestión de respuestas de anfitriones a comentarios")
public class RespuestaComentarioController {

    @GetMapping
    @Operation(summary = "Listar todas las respuestas",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de respuestas obtenida",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = RespuestaComentarioDTO.class)),
                                    examples = @ExampleObject(value = "[{\"id\":1,\"idComentario\":3,\"idAnfitrion\":5,\"texto\":\"Gracias por tu comentario\",\"fechaRespuesta\":\"2025-03-01T12:00:00\"}]"))),
                    @ApiResponse(responseCode = "204", description = "No hay respuestas registradas"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<List<RespuestaComentarioDTO>> getAll() {
        return ResponseEntity.ok(Collections.emptyList()); // mock
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una respuesta por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Respuesta encontrada",
                            content = @Content(schema = @Schema(implementation = RespuestaComentarioDTO.class),
                                    examples = @ExampleObject(value = "{\"id\":1,\"idComentario\":3,\"idAnfitrion\":5,\"texto\":\"Gracias por tu comentario\",\"fechaRespuesta\":\"2025-03-01T12:00:00\"}"))),
                    @ApiResponse(responseCode = "404", description = "Respuesta no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<RespuestaComentarioDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new RespuestaComentarioDTO()); // mock
    }

    @PostMapping
    @Operation(summary = "Crear una nueva respuesta a comentario",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RespuestaComentarioDTO.class),
                            examples = @ExampleObject(value = "{\"idComentario\":3,\"idAnfitrion\":5,\"texto\":\"Agradecemos tu visita\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Respuesta creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Comentario no encontrado")
            })
    public ResponseEntity<String> create(@RequestBody RespuestaComentarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Respuesta creada (mock)");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una respuesta existente",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"texto\":\"Hemos mejorado el servicio, gracias por tu comentario\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Respuesta actualizada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Respuesta no encontrada")
            })
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody RespuestaComentarioDTO dto) {
        return ResponseEntity.ok("Respuesta actualizada (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una respuesta a comentario",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Respuesta eliminada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Respuesta no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar respuesta")
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
