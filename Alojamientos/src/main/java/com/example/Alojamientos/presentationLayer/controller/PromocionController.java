package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.PromocionDTO;
import com.example.Alojamientos.businessLayer.service.PromocionService;
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
@RequestMapping("/api/promociones")
@Tag(name = "Promociones", description = "Gestión de promociones para los alojamientos")
@RequiredArgsConstructor
public class PromocionController {

    private final PromocionService promocionService;

    // ============================================================
    // Listar todas las promociones activas de un alojamiento
    // ============================================================
    @GetMapping("/alojamiento/{lodgingId}")
    @Operation(summary = "Listar promociones activas de un alojamiento",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de promociones obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PromocionDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "ID de alojamiento inválido"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado")
            })
    public ResponseEntity<?> listarPorAlojamiento(@PathVariable Integer lodgingId) {
        try {
            if (lodgingId == null || lodgingId <= 0) {
                return ResponseEntity.badRequest().body("ID de alojamiento inválido"); // 400
            }

            List<PromocionDTO> lista = promocionService.listarPromocionesActivas(lodgingId);

            if (lista.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No hay promociones activas para el alojamiento"); // 404
            }

            return ResponseEntity.ok(lista); // 200
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al listar promociones"); // opcional log
        }
    }

    // ============================================================
    // Obtener promoción por ID
    // ============================================================
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una promoción por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promoción encontrada",
                            content = @Content(schema = @Schema(implementation = PromocionDTO.class))),
                    @ApiResponse(responseCode = "400", description = "ID inválido"),
                    @ApiResponse(responseCode = "404", description = "Promoción no encontrada")
            })
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("ID inválido"); // 400
            }

            PromocionDTO dto = promocionService.obtenerPorId(id);
            return ResponseEntity.ok(dto); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    // ============================================================
    // Crear nueva promoción
    // ============================================================
    @PostMapping
    @Operation(summary = "Crear una nueva promoción",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PromocionDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Promoción creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado")
            })
    public ResponseEntity<?> create(@Valid @RequestBody PromocionDTO dto) {
        try {
            PromocionDTO nuevo = promocionService.crearPromocion(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo); // 201
        } catch (IllegalArgumentException e) {
            if (e.getMessage().toLowerCase().contains("alojamiento")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            }
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }

    // ============================================================
    // Actualizar promoción
    // ============================================================
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una promoción existente",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PromocionDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promoción actualizada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Promoción no encontrada")
            })
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody PromocionDTO dto) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("ID inválido"); // 400
            }

            PromocionDTO actualizado = promocionService.actualizarPromocion(id, dto);
            return ResponseEntity.ok(actualizado); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    // ============================================================
    // Desactivar promoción
    // ============================================================
    @PutMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar una promoción",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promoción desactivada correctamente"),
                    @ApiResponse(responseCode = "400", description = "ID inválido"),
                    @ApiResponse(responseCode = "404", description = "Promoción no encontrada")
            })
    public ResponseEntity<?> desactivar(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("ID inválido"); // 400
            }

            promocionService.desactivarPromocion(id);
            return ResponseEntity.ok("Promoción desactivada correctamente"); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    // ============================================================
    // Buscar promoción por código
    // ============================================================
    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Buscar promoción por código promocional",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promoción encontrada",
                            content = @Content(schema = @Schema(implementation = PromocionDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Código inválido"),
                    @ApiResponse(responseCode = "404", description = "Código de promoción no válido o expirado")
            })
    public ResponseEntity<?> buscarPorCodigo(@PathVariable String codigo) {
        try {
            if (codigo == null || codigo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Código inválido"); // 400
            }

            PromocionDTO dto = promocionService.buscarPorCodigo(codigo);
            return ResponseEntity.ok(dto); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }
}

