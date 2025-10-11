package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.RespuestaComentarioDTO;
import com.example.Alojamientos.businessLayer.service.RespuestaComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/respuestas-comentarios")
@Tag(name = "Respuestas a Comentarios", description = "Gestión de respuestas de anfitriones a comentarios")
@RequiredArgsConstructor
public class RespuestaComentarioController {

    private final RespuestaComentarioService respuestaService;

    // ============================================================
    // Listar respuestas de un comentario
    // ============================================================
    @GetMapping("/comentario/{commentId}")
    @Operation(summary = "Listar respuestas de un comentario",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Respuestas obtenidas correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = RespuestaComentarioDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "ID de comentario inválido"),
                    @ApiResponse(responseCode = "404", description = "No se encontraron respuestas para el comentario")
            })
    public ResponseEntity<?> listarPorComentario(@PathVariable Integer commentId) {
        if (commentId == null || commentId <= 0) {
            return ResponseEntity.badRequest().body("ID de comentario inválido"); // 400
        }
        List<RespuestaComentarioDTO> lista = respuestaService.listarPorComentario(commentId);
        if (lista.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontraron respuestas para el comentario"); // 404
        }
        return ResponseEntity.ok(lista); // 200
    }

    // ============================================================
    // Obtener respuesta por ID
    // ============================================================
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una respuesta por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Respuesta encontrada",
                            content = @Content(schema = @Schema(implementation = RespuestaComentarioDTO.class))),
                    @ApiResponse(responseCode = "400", description = "ID inválido"),
                    @ApiResponse(responseCode = "404", description = "Respuesta no encontrada")
            })
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID inválido"); // 400
        }
        try {
            RespuestaComentarioDTO dto = respuestaService.obtenerPorId(id);
            return ResponseEntity.ok(dto); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    // ============================================================
    // Crear nueva respuesta
    // ============================================================
    @PostMapping
    @Operation(summary = "Crear una nueva respuesta a comentario",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RespuestaComentarioDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Respuesta creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Comentario no encontrado")
            })
    public ResponseEntity<?> create(@Valid @RequestBody RespuestaComentarioDTO dto) {
        try {
            RespuestaComentarioDTO nueva = respuestaService.crearRespuesta(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva); // 201
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("comentario no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            }
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }

    // ============================================================
    // Actualizar respuesta existente
    // ============================================================
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una respuesta existente",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"text\":\"Actualización del texto de la respuesta\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Respuesta actualizada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Respuesta no encontrada")
            })
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody RespuestaComentarioDTO dto) {
        if (dto.getText() == null || dto.getText().isBlank()) {
            return ResponseEntity.badRequest().body("El texto de la respuesta no puede estar vacío"); // 400
        }
        try {
            RespuestaComentarioDTO updated = respuestaService.actualizarRespuesta(id, dto.getText());
            return ResponseEntity.ok(updated); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    // ============================================================
    // Eliminar respuesta
    // ============================================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una respuesta a comentario",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Respuesta eliminada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Respuesta no encontrada"),
                    @ApiResponse(responseCode = "400", description = "ID inválido")
            })
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID inválido"); // 400
        }
        try {
            respuestaService.eliminarRespuesta(id);
            return ResponseEntity.noContent().build(); // 204
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }
}

