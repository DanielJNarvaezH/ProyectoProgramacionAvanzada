package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.AlojamientoDTO;
import com.example.Alojamientos.businessLayer.service.AlojamientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alojamientos")
@Tag(name = "Alojamiento", description = "Gestión de alojamientos (casas, apartamentos, habitaciones, etc.)")
@RequiredArgsConstructor
public class AlojamientoController {

    private final AlojamientoService alojamientoService;

    // ============================================================
    // RF9, HU-009: Crear alojamiento
    // ============================================================
    @PostMapping
    @Operation(summary = "Crear un nuevo alojamiento",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AlojamientoDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Alojamiento creado exitosamente",
                            content = @Content(schema = @Schema(implementation = AlojamientoDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "409", description = "Conflicto: ya existe un alojamiento similar")
            })
    public ResponseEntity<?> crearAlojamiento(@Valid @RequestBody AlojamientoDTO dto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errores = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(Map.of("errors", errores));
        }

        try {
            AlojamientoDTO creado = alojamientoService.crearAlojamiento(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("ya existe") || msg.contains("existe")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ============================================================
    // RF10, HU-010: Actualizar alojamiento
    // ============================================================
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un alojamiento existente",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AlojamientoDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Alojamiento actualizado correctamente",
                            content = @Content(schema = @Schema(implementation = AlojamientoDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado")
            })
    public ResponseEntity<?> actualizarAlojamiento(@PathVariable Integer id,
                                                   @Valid @RequestBody AlojamientoDTO dto,
                                                   BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errores = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(Map.of("errors", errores));
        }

        try {
            AlojamientoDTO actualizado = alojamientoService.actualizarAlojamiento(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("no encontrado") || msg.contains("no se puede actualizar")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ============================================================
    // RF11, HU-011: Eliminar alojamiento (soft delete)
    // ============================================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un alojamiento existente",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Alojamiento eliminado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado"),
                    @ApiResponse(responseCode = "400", description = "No se puede eliminar por reservas activas")
            })
    public ResponseEntity<?> eliminarAlojamiento(@PathVariable Integer id) {
        try {
            alojamientoService.eliminarAlojamiento(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ============================================================
    // RF12, HU-012: Listar alojamientos de un anfitrión
    // ============================================================
    @GetMapping("/anfitrion/{hostId}")
    @Operation(summary = "Listar alojamientos de un anfitrión específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de alojamientos obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlojamientoDTO.class)))),
                    @ApiResponse(responseCode = "204", description = "El anfitrión no tiene alojamientos registrados"),
                    @ApiResponse(responseCode = "400", description = "ID de anfitrión inválido")
            })
    public ResponseEntity<?> listarPorAnfitrion(@PathVariable Integer hostId) {
        if (hostId == null || hostId <= 0) {
            return ResponseEntity.badRequest().body("El ID del anfitrión es inválido");
        }

        List<AlojamientoDTO> alojamientos = alojamientoService.listarPorAnfitrion(hostId);
        if (alojamientos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(alojamientos);
    }

    // ============================================================
    // RF14, HU-014: Buscar alojamientos por ciudad
    // ============================================================
    @GetMapping("/buscar")
    @Operation(summary = "Buscar alojamientos por ciudad",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultados obtenidos correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlojamientoDTO.class)))),
                    @ApiResponse(responseCode = "204", description = "No se encontraron alojamientos en la ciudad especificada"),
                    @ApiResponse(responseCode = "400", description = "Nombre de ciudad inválido")
            })
    public ResponseEntity<?> buscarPorCiudad(@RequestParam String ciudad) {
        if (ciudad == null || ciudad.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Debe especificar una ciudad válida");
        }

        List<AlojamientoDTO> resultados = alojamientoService.buscarPorCiudad(ciudad);
        if (resultados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(resultados);
    }

    // ============================================================
    // RF16, HU-016: Filtrar por rango de precio
    // ============================================================
    @GetMapping("/filtro/precio")
    @Operation(summary = "Filtrar alojamientos por rango de precio",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultados filtrados correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlojamientoDTO.class)))),
                    @ApiResponse(responseCode = "204", description = "No se encontraron alojamientos en ese rango de precios"),
                    @ApiResponse(responseCode = "400", description = "Parámetros de precio inválidos")
            })
    public ResponseEntity<?> filtrarPorRangoPrecio(@RequestParam Double min, @RequestParam Double max) {
        if (min == null || max == null || min < 0 || max < min) {
            return ResponseEntity.badRequest().body("Rango de precios inválido");
        }

        List<AlojamientoDTO> resultados = alojamientoService.buscarPorRangoPrecio(min, max);
        if (resultados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(resultados);
    }

    // ============================================================
    // RF18, HU-018: Obtener alojamiento por ID
    // ============================================================
    @GetMapping("/{id}")
    @Operation(summary = "Obtener alojamiento por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Alojamiento encontrado",
                            content = @Content(schema = @Schema(implementation = AlojamientoDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado"),
                    @ApiResponse(responseCode = "400", description = "ID inválido")
            })
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("El ID del alojamiento es inválido");
        }

        try {
            AlojamientoDTO dto = alojamientoService.obtenerPorId(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ============================================================
    // RF18, RN26: Listar todos los alojamientos activos
    // ============================================================
    @GetMapping("/activos")
    @Operation(summary = "Listar todos los alojamientos activos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlojamientoDTO.class)))),
                    @ApiResponse(responseCode = "204", description = "No hay alojamientos activos")
            })
    public ResponseEntity<?> listarActivos() {
        List<AlojamientoDTO> activos = alojamientoService.listarActivos();

        if (activos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(activos);
    }





    // ============================================================
    // RF13, HU-013: Obtener métricas de un alojamiento
    // ============================================================
    @GetMapping("/{id}/metricas/reservas")
    @Operation(summary = "Obtener el número de reservas de un alojamiento",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Número de reservas obtenido correctamente",
                            content = @Content(schema = @Schema(implementation = Long.class))),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado"),
                    @ApiResponse(responseCode = "400", description = "ID inválido")
            })
    public ResponseEntity<?> obtenerNumeroReservas(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("El ID del alojamiento es inválido");
        }


        try {
            Long reservas = alojamientoService.obtenerNumeroReservas(id);
            return ResponseEntity.ok(Map.of("numeroReservas", reservas));
        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }
}
