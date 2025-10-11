package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.ImagenDTO;
import com.example.Alojamientos.businessLayer.service.ImagenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/imagenes")
@Tag(name = "Imágenes", description = "Gestión de imágenes de alojamientos")
@RequiredArgsConstructor
public class ImagenController {

    private final ImagenService imagenService;

    // ============================================================
    // RF10, RN8: Crear imagen para alojamiento
    // ============================================================
    @PostMapping
    @Operation(summary = "Subir una nueva imagen para un alojamiento",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ImagenDTO.class),
                            examples = @ExampleObject(value = "{\"lodgingId\":5,\"url\":\"https://ejemplo.com/img2.jpg\",\"description\":\"Habitación principal\",\"order\":2}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Imagen creada exitosamente",
                            content = @Content(schema = @Schema(implementation = ImagenDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos o se superó el límite de 10 imágenes"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado")
            })
    public ResponseEntity<?> crearImagen(@Valid @RequestBody ImagenDTO dto) {
        try {
            ImagenDTO creada = imagenService.crearImagen(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ============================================================
    // Listar imágenes de un alojamiento (ordenadas)
    // ============================================================
    @GetMapping("/alojamiento/{alojamientoId}")
    @Operation(summary = "Listar imágenes de un alojamiento específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de imágenes obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ImagenDTO.class)),
                                    examples = @ExampleObject(value = "[{\"lodgingId\":5,\"url\":\"https://ejemplo.com/img1.jpg\",\"description\":\"Vista frontal\",\"order\":1}]"))),
                    @ApiResponse(responseCode = "204", description = "El alojamiento no tiene imágenes registradas"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado")
            })
    public ResponseEntity<?> listarPorAlojamiento(@PathVariable Integer alojamientoId) {
        try {
            List<ImagenDTO> lista = imagenService.listarPorAlojamiento(alojamientoId);
            if (lista.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204
            }
            return ResponseEntity.ok(lista); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    // ============================================================
    // Obtener imagen por ID
    // ============================================================
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una imagen por su ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Imagen encontrada correctamente",
                            content = @Content(schema = @Schema(implementation = ImagenDTO.class),
                                    examples = @ExampleObject(value = "{\"lodgingId\":5,\"url\":\"https://ejemplo.com/img1.jpg\",\"description\":\"Vista frontal\",\"order\":1}"))),
                    @ApiResponse(responseCode = "404", description = "Imagen no encontrada"),
                    @ApiResponse(responseCode = "400", description = "ID inválido o mal formado")
            })
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            ImagenDTO imagen = imagenService.obtenerPorId(id);
            return ResponseEntity.ok(imagen); // 200
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            }
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }

    // ============================================================
    // Actualizar imagen
    // ============================================================
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar la descripción u orden de una imagen existente",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ImagenDTO.class),
                            examples = @ExampleObject(value = "{\"description\":\"Nueva descripción\",\"order\":3}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Imagen actualizada correctamente",
                            content = @Content(schema = @Schema(implementation = ImagenDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Imagen no encontrada")
            })
    public ResponseEntity<?> actualizarImagen(@PathVariable Integer id, @RequestBody ImagenDTO dto) {
        try {
            ImagenDTO actualizada = imagenService.actualizarImagen(id, dto);
            return ResponseEntity.ok(actualizada); // 200
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            }
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }

    // ============================================================
    // Eliminar imagen
    // ============================================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una imagen por su ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Imagen eliminada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Imagen no encontrada"),
                    @ApiResponse(responseCode = "400", description = "ID inválido o mal formado")
            })
    public ResponseEntity<?> eliminarImagen(@PathVariable Integer id) {
        try {
            imagenService.eliminarImagen(id);
            return ResponseEntity.noContent().build(); // 204
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            }
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }
}

