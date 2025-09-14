package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.ImagenDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/imagenes")
@Tag(name = "Imágenes", description = "Gestión de imágenes de alojamientos")
public class ImagenController {

    @GetMapping
    @Operation(summary = "Listar todas las imágenes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de imágenes obtenida",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ImagenDTO.class)),
                                    examples = @ExampleObject(value = "[{\"id\":1,\"idAlojamiento\":5,\"url\":\"https://ejemplo.com/img1.jpg\",\"descripcion\":\"Vista frontal\",\"ordenVisualizacion\":1}]"))),
                    @ApiResponse(responseCode = "204", description = "No hay imágenes registradas"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<List<ImagenDTO>> getAll() {
        return ResponseEntity.ok(Collections.emptyList()); // mock
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una imagen por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Imagen encontrada",
                            content = @Content(schema = @Schema(implementation = ImagenDTO.class),
                                    examples = @ExampleObject(value = "{\"id\":1,\"idAlojamiento\":5,\"url\":\"https://ejemplo.com/img1.jpg\",\"descripcion\":\"Vista frontal\",\"ordenVisualizacion\":1}"))),
                    @ApiResponse(responseCode = "404", description = "Imagen no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<ImagenDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ImagenDTO()); // mock
    }

    @PostMapping
    @Operation(summary = "Subir una nueva imagen",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ImagenDTO.class),
                            examples = @ExampleObject(value = "{\"idAlojamiento\":5,\"url\":\"https://ejemplo.com/img2.jpg\",\"descripcion\":\"Habitación principal\",\"ordenVisualizacion\":2}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Imagen creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado")
            })
    public ResponseEntity<String> create(@RequestBody ImagenDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Imagen creada (mock)");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una imagen existente",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"descripcion\":\"Nueva descripción\",\"ordenVisualizacion\":3}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Imagen actualizada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Imagen no encontrada")
            })
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody ImagenDTO dto) {
        return ResponseEntity.ok("Imagen actualizada (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una imagen",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Imagen eliminada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Imagen no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar imagen")
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
