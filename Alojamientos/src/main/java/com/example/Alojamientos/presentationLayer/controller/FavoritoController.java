package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.FavoritoDTO;
import com.example.Alojamientos.businessLayer.service.FavoritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Gestión de alojamientos favoritos de los usuarios")
@RequiredArgsConstructor
public class FavoritoController {

    private final FavoritoService favoritoService;

    // ============================================================
// RF36, HU-031: Marcar alojamiento como favorito
// ============================================================
    @PostMapping
    @Operation(summary = "Agregar un alojamiento a favoritos",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FavoritoDTO.class),
                            examples = @ExampleObject(value = "{\"userId\":7,\"lodgingId\":12}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Favorito agregado correctamente",
                            content = @Content(schema = @Schema(implementation = FavoritoDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos o favorito ya existente"),
                    @ApiResponse(responseCode = "404", description = "Usuario o alojamiento no encontrado")
            })
    public ResponseEntity<?> agregarFavorito(@Valid @RequestBody FavoritoDTO dto) {
        try {
            FavoritoDTO nuevo = favoritoService.agregarFavorito(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // ============================================================
// RF37, HU-032: Listar favoritos de un usuario
// ============================================================
    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar favoritos de un usuario específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de favoritos obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = FavoritoDTO.class)))),
                    @ApiResponse(responseCode = "204", description = "El usuario no tiene favoritos"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            })
    public ResponseEntity<?> listarPorUsuario(@PathVariable Integer usuarioId) {
        try {
            List<FavoritoDTO> lista = favoritoService.listarPorUsuario(usuarioId);
            if (lista.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204
            }
            return ResponseEntity.ok(lista); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }


    // ============================================================
    // RF38, HU-033: Contar favoritos de un alojamiento
    // ============================================================
    @GetMapping("/alojamiento/{alojamientoId}/count")
    @Operation(summary = "Contar favoritos de un alojamiento",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cantidad de favoritos obtenida correctamente",
                            content = @Content(schema = @Schema(implementation = Long.class),
                                    examples = @ExampleObject(value = "15"))),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<?> contarFavoritos(@PathVariable Integer alojamientoId) {
        try {
            Long count = favoritoService.contarFavoritosPorAlojamiento(alojamientoId);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al contar los favoritos del alojamiento");
        }
    }

    // ============================================================
    // Eliminar un favorito por ID
    // ============================================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un favorito por ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Favorito eliminado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Favorito no encontrado"),
                    @ApiResponse(responseCode = "409", description = "Error de conflicto al eliminar favorito")
            })
    public ResponseEntity<?> eliminarFavorito(@PathVariable Integer id) {
        try {
            favoritoService.eliminarFavorito(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se pudo eliminar el favorito por conflicto interno");
        }
    }

    // ============================================================
    // Eliminar favorito por usuario y alojamiento
    // ============================================================
    @DeleteMapping("/usuario/{usuarioId}/alojamiento/{alojamientoId}")
    @Operation(summary = "Eliminar un favorito específico por usuario y alojamiento",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Favorito eliminado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Favorito no encontrado"),
                    @ApiResponse(responseCode = "409", description = "Error de conflicto al eliminar favorito")
            })
    public ResponseEntity<?> eliminarFavoritoPorUsuarioYAlojamiento(
            @PathVariable Integer usuarioId, @PathVariable Integer alojamientoId) {
        try {
            favoritoService.eliminarFavoritoPorUsuarioYAlojamiento(usuarioId, alojamientoId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Error interno al eliminar el favorito");
        }
    }

    // ============================================================
    // Verificar si un alojamiento es favorito de un usuario
    // ============================================================
    @GetMapping("/usuario/{usuarioId}/alojamiento/{alojamientoId}")
    @Operation(summary = "Verificar si un alojamiento es favorito de un usuario",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultado de verificación obtenido",
                            content = @Content(schema = @Schema(implementation = Boolean.class),
                                    examples = @ExampleObject(value = "true"))),
                    @ApiResponse(responseCode = "404", description = "Usuario o alojamiento no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<?> esFavorito(
            @PathVariable Integer usuarioId,
            @PathVariable Integer alojamientoId) {
        try {
            boolean resultado = favoritoService.esFavorito(usuarioId, alojamientoId);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al verificar si el alojamiento es favorito");
        }
    }
}

