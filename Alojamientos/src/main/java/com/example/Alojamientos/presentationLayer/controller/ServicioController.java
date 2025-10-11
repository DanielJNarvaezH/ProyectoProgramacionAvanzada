package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.ServicioDTO;
import com.example.Alojamientos.businessLayer.service.ServicioService;
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
@RequestMapping("/api/servicios")
@Tag(name = "Servicios", description = "Gestión de servicios disponibles en alojamientos")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    // ============================================================
// Listar servicios activos
// ============================================================
    @GetMapping
    @Operation(summary = "Listar todos los servicios activos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de servicios obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServicioDTO.class)))),
                    @ApiResponse(responseCode = "404", description = "No hay servicios registrados"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<?> getAll() {
        try {
            List<ServicioDTO> lista = servicioService.listarActivos();
            if (lista.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No hay servicios registrados"); // 404
            }
            return ResponseEntity.ok(lista); // 200
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al listar servicios"); // 500
        }
    }


    // ============================================================
    // Obtener servicio por ID
    // ============================================================
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un servicio por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Servicio encontrado",
                            content = @Content(schema = @Schema(implementation = ServicioDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Servicio no encontrado"),
                    @ApiResponse(responseCode = "400", description = "ID inválido")
            })
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID inválido"); // 400
        }
        try {
            ServicioDTO dto = servicioService.obtenerPorId(id);
            return ResponseEntity.ok(dto); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    // ============================================================
    // Crear nuevo servicio
    // ============================================================
    @PostMapping
    @Operation(summary = "Crear un nuevo servicio",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServicioDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Servicio creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "409", description = "Ya existe un servicio con ese nombre")
            })
    public ResponseEntity<?> create(@Valid @RequestBody ServicioDTO dto) {
        try {
            ServicioDTO nuevo = servicioService.crearServicio(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo); // 201
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("ya existe un servicio")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409
            }
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }

    // ============================================================
    // Actualizar servicio existente
    // ============================================================
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un servicio",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServicioDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Servicio actualizado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
            })
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody ServicioDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            return ResponseEntity.badRequest().body("El nombre del servicio es obligatorio"); // 400
        }
        try {
            ServicioDTO updated = servicioService.actualizarServicio(id, dto);
            return ResponseEntity.ok(updated); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    // ============================================================
    // Desactivar servicio (soft delete)
    // ============================================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar un servicio",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Servicio desactivado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Servicio no encontrado"),
                    @ApiResponse(responseCode = "400", description = "ID inválido")
            })
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID inválido"); // 400
        }
        try {
            servicioService.desactivarServicio(id);
            return ResponseEntity.noContent().build(); // 204
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    // ============================================================
    // Buscar servicio por nombre
    // ============================================================
    @GetMapping("/buscar")
    @Operation(summary = "Buscar servicio por nombre",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Servicio encontrado",
                            content = @Content(schema = @Schema(implementation = ServicioDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Nombre inválido"),
                    @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
            })
    public ResponseEntity<?> buscarPorNombre(@RequestParam String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return ResponseEntity.badRequest().body("Nombre inválido"); // 400
        }
        try {
            ServicioDTO dto = servicioService.buscarPorNombre(nombre);
            return ResponseEntity.ok(dto); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }
}

