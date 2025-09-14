package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.FavoritoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Gestión de alojamientos favoritos de los usuarios")
public class FavoritoController {

    @GetMapping
    @Operation(summary = "Listar todos los favoritos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de favoritos obtenida",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = FavoritoDTO.class)),
                                    examples = @ExampleObject(value = "[{\"id\":1,\"idUsuario\":7,\"idAlojamiento\":12,\"fechaAgregado\":\"2025-03-10T09:30:00\"}]"))),
                    @ApiResponse(responseCode = "204", description = "No hay favoritos registrados"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<List<FavoritoDTO>> getAll() {
        return ResponseEntity.ok(Collections.emptyList()); // mock
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un favorito por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Favorito encontrado",
                            content = @Content(schema = @Schema(implementation = FavoritoDTO.class),
                                    examples = @ExampleObject(value = "{\"id\":1,\"idUsuario\":7,\"idAlojamiento\":12,\"fechaAgregado\":\"2025-03-10T09:30:00\"}"))),
                    @ApiResponse(responseCode = "404", description = "Favorito no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<FavoritoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new FavoritoDTO()); // mock
    }

    @PostMapping
    @Operation(summary = "Agregar un nuevo favorito",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FavoritoDTO.class),
                            examples = @ExampleObject(value = "{\"idUsuario\":7,\"idAlojamiento\":12}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Favorito agregado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Usuario o alojamiento no encontrado")
            })
    public ResponseEntity<String> create(@RequestBody FavoritoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Favorito agregado (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un favorito",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Favorito eliminado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Favorito no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar favorito")
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
