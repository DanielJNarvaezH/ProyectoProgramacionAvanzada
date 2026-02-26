package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.CodigoRecuperacionDTO;
import com.example.Alojamientos.businessLayer.service.CodigoRecuperacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/codigos-recuperacion")
@Tag(name = "Códigos de Recuperación", description = "Gestión de códigos de recuperación de contraseña")
@RequiredArgsConstructor
public class CodigoRecuperacionController {

    private final CodigoRecuperacionService codigoService;

    @PostMapping("/{usuarioId}")
    @Operation(
            summary = "Generar un nuevo código de recuperación",
            description = "Crea un nuevo código de recuperación de contraseña para un usuario. (RF5, HU-005, RN7)",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Código generado correctamente",
                            content = @Content(schema = @Schema(implementation = CodigoRecuperacionDTO.class),
                                    examples = @ExampleObject(value = "{\"userId\":1,\"code\":\"456123\",\"expirationDate\":\"2025-10-10T18:30:00\",\"used\":false}"))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno al generar el código")
            }
    )
    public ResponseEntity<?> generarCodigo(@PathVariable Integer usuarioId) {
        try {
            if (usuarioId == null || usuarioId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID de usuario inválido.");
            }
            CodigoRecuperacionDTO dto = codigoService.generarCodigo(usuarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar el código: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener un código por ID",
            description = "Devuelve la información de un código específico.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Código encontrado",
                            content = @Content(schema = @Schema(implementation = CodigoRecuperacionDTO.class),
                                    examples = @ExampleObject(value = "{\"userId\":1,\"code\":\"789456\",\"expirationDate\":\"2025-10-10T19:00:00\",\"used\":false}"))),
                    @ApiResponse(responseCode = "404", description = "Código no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            }
    )
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            CodigoRecuperacionDTO dto = codigoService.obtenerPorId(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el código: " + e.getMessage());
        }
    }

    @PostMapping("/validar")
    @Operation(
            summary = "Validar código de recuperación",
            description = "Valida si un código de recuperación es correcto y sigue vigente.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"userId\":1,\"code\":\"123456\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Código válido y vigente"),
                    @ApiResponse(responseCode = "400", description = "Código inválido o expirado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            }
    )
    public ResponseEntity<?> validarCodigo(@RequestBody CodigoRecuperacionDTO dto) {
        try {
            boolean valido = codigoService.validarCodigo(dto.getCode(), dto.getUserId());
            if (!valido) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Código inválido, usado o expirado.");
            }
            return ResponseEntity.ok("Código válido y vigente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al validar el código: " + e.getMessage());
        }
    }

    @PutMapping("/marcar-usado")
    @Operation(
            summary = "Marcar un código como usado",
            description = "Permite marcar un código válido como utilizado después de cambiar la contraseña.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"userId\":1,\"code\":\"987654\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Código marcado como usado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Código no válido o no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno al marcar el código")
            }
    )
    public ResponseEntity<?> marcarComoUsado(@RequestBody CodigoRecuperacionDTO dto) {
        try {
            codigoService.marcarComoUsado(dto.getCode(), dto.getUserId());
            return ResponseEntity.ok("Código marcado como usado correctamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al marcar el código como usado: " + e.getMessage());
        }
    }

    @DeleteMapping("/limpiar")
    @Operation(
            summary = "Eliminar códigos expirados",
            description = "Limpia todos los códigos de recuperación expirados del sistema.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Códigos expirados eliminados correctamente"),
                    @ApiResponse(responseCode = "405", description = "Method Not Allowed\n"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar códigos")
            }
    )
    public ResponseEntity<?> limpiarCodigosExpirados() {
        try {
            // Verificación simple (puedes omitirla si no aplica tu lógica de validación)
            if (java.time.LocalDateTime.now() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Fecha actual no válida para la limpieza.");
            }

            // Lógica de eliminación de códigos expirados
            codigoService.limpiarCodigosExpirados();

            return ResponseEntity.noContent().build(); // ✅ ÉXITO (204)
        } catch (IllegalArgumentException e) {
            // Error por datos inválidos o parámetros incorrectos
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Solicitud inválida: " + e.getMessage());
        } catch (Exception e) {
            // Error interno inesperado
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar códigos expirados: " + e.getMessage());
        }
    }

}

