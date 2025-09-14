package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.ServicioDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/servicios")
@Tag(name = "Servicios", description = "Gestión de servicios disponibles en alojamientos")
public class ServicioController {

    @GetMapping
    @Operation(summary = "Listar todos los servicios",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de servicios obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServicioDTO.class)),
                                    examples = @ExampleObject(value = "[{\"id\":1,\"nombre\":\"WiFi\",\"descripcion\":\"Internet de alta velocidad\"}]"))),
                    @ApiResponse(responseCode = "204", description = "No hay servicios registrados"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<List<ServicioDTO>> getAll() {
        return ResponseEntity.ok(Collections.emptyList()); // mock
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un servicio por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Servicio encontrado",
                            content = @Content(schema = @Schema(implementation = ServicioDTO.class),
                                    examples = @ExampleObject(value = "{\"id\":1,\"nombre\":\"WiFi\",\"descripcion\":\"Internet de alta velocidad\"}"))),
                    @ApiResponse(responseCode = "404", description = "Servicio no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<ServicioDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ServicioDTO()); // mock
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo servicio",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServicioDTO.class),
                            examples = @ExampleObject(value = "{\"nombre\":\"Parqueadero\",\"descripcion\":\"Parqueadero privado para huéspedes\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Servicio creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "409", description = "Ya existe un servicio con ese nombre")
            })
    public ResponseEntity<String> create(@RequestBody ServicioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Servicio creado (mock)");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un servicio",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"nombre\":\"Piscina\",\"descripcion\":\"Piscina climatizada\"}"))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Servicio actualizado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
            })
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody ServicioDTO dto) {
        return ResponseEntity.ok("Servicio actualizado (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un servicio",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Servicio eliminado"),
                    @ApiResponse(responseCode = "404", description = "Servicio no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar servicio")
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
