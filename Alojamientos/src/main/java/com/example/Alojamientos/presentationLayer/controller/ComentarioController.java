package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.ComentarioDTO;
import com.example.Alojamientos.businessLayer.service.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
@Tag(name = "Comentarios", description = "Gestión de comentarios en alojamientos")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    // -------------------- CREAR COMENTARIO --------------------
    @PostMapping
    @Operation(
            summary = "Crear un nuevo comentario",
            description = "Permite a un usuario crear un comentario sobre una reserva completada.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ComentarioDTO.class),
                            examples = @ExampleObject(value = "{\"reservationId\":3,\"userId\":1,\"rating\":5,\"text\":\"Excelente alojamiento!\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Comentario creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos o reserva no completada"),
                    @ApiResponse(responseCode = "404", description = "Reserva o usuario no encontrado")
            }
    )
    public ResponseEntity<?> crearComentario(@RequestBody ComentarioDTO dto) {
        try {
            ComentarioDTO creado = comentarioService.crearComentario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().toLowerCase().contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No fue posible procesar la creación del comentario: " + e.getMessage());
        }
    }

    // -------------------- LISTAR POR ALOJAMIENTO --------------------
    @GetMapping("/alojamiento/{alojamientoId}")
    @Operation(
            summary = "Listar comentarios de un alojamiento",
            description = "Devuelve los comentarios asociados a un alojamiento, ordenados por fecha descendente.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de comentarios obtenida",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComentarioDTO.class)))),
                    @ApiResponse(responseCode = "404", description = "No se encontraron comentarios para este alojamiento"),
                    @ApiResponse(responseCode = "400", description = "Error al procesar la solicitud de comentarios")
            }
    )
    public ResponseEntity<?> listarPorAlojamiento(@PathVariable Integer alojamientoId) {
        try {
            List<ComentarioDTO> comentarios = comentarioService.listarPorAlojamiento(alojamientoId);
            if (comentarios.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontraron comentarios para este alojamiento.");
            }
            return ResponseEntity.ok(comentarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al procesar la solicitud de comentarios: " + e.getMessage());
        }
    }

    // -------------------- OBTENER POR ID --------------------
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener un comentario por ID",
            description = "Devuelve la información de un comentario específico.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Comentario encontrado",
                            content = @Content(schema = @Schema(implementation = ComentarioDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Comentario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Error al procesar la búsqueda del comentario")
            }
    )
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            ComentarioDTO comentario = comentarioService.obtenerPorId(id);
            return ResponseEntity.ok(comentario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al procesar la búsqueda del comentario: " + e.getMessage());
        }
    }

    // -------------------- OBTENER PROMEDIO --------------------
    @GetMapping("/alojamiento/{alojamientoId}/promedio")
    @Operation(
            summary = "Obtener promedio de calificaciones",
            description = "Calcula el promedio de calificaciones de un alojamiento.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promedio calculado exitosamente",
                            content = @Content(schema = @Schema(example = "4.5"))),
                    @ApiResponse(responseCode = "404", description = "Alojamiento sin comentarios"),
                    @ApiResponse(responseCode = "400", description = "No se pudo calcular el promedio de calificaciones")
            }
    )
    public ResponseEntity<?> obtenerPromedio(@PathVariable Integer alojamientoId) {
        try {
            Double promedio = comentarioService.obtenerPromedioCalificaciones(alojamientoId);
            if (promedio == 0.0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontraron comentarios para este alojamiento.");
            }
            return ResponseEntity.ok(promedio);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No se pudo calcular el promedio de calificaciones: " + e.getMessage());
        }
    }

    // -------------------- ACTUALIZAR --------------------
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un comentario existente",
            description = "Permite modificar el texto de un comentario existente.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"text\":\"Excelente experiencia, volvería sin dudarlo.\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Comentario actualizado correctamente",
                            content = @Content(schema = @Schema(implementation = ComentarioDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Texto inválido o demasiado largo"),
                    @ApiResponse(responseCode = "404", description = "Comentario no encontrado")
            }
    )
    public ResponseEntity<?> actualizarComentario(@PathVariable Integer id, @RequestBody ComentarioDTO dto) {
        try {
            ComentarioDTO actualizado = comentarioService.actualizarComentario(id, dto.getText());
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().toLowerCase().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // -------------------- ELIMINAR --------------------
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un comentario",
            description = "Permite eliminar un comentario existente (solo administradores o moderadores).",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Comentario eliminado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Comentario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "No fue posible eliminar el comentario")
            }
    )
    public ResponseEntity<?> eliminarComentario(@PathVariable Integer id) {
        try {
            comentarioService.eliminarComentario(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No fue posible eliminar el comentario: " + e.getMessage());
        }
    }
}

