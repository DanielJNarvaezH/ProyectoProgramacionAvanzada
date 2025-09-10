package com.example.Alojamientos.controller;

import com.example.Alojamientos.dto.UsuarioDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
public class UsuarioController {

    @GetMapping
    @Operation(summary = "Listar todos los usuarios",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioDTO.class)),
                                    examples = @ExampleObject(value = "[{\"id\":1,\"nombre\":\"Juan Perez\",\"email\":\"juan@correo.com\"}]"))),
                    @ApiResponse(responseCode = "204", description = "No hay usuarios registrados"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<List<UsuarioDTO>> getAll() {
        return ResponseEntity.ok(Collections.emptyList()); // mock
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                            content = @Content(schema = @Schema(implementation = UsuarioDTO.class),
                                    examples = @ExampleObject(value = "{\"id\":1,\"nombre\":\"Juan Perez\",\"email\":\"juan@correo.com\"}"))),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<UsuarioDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new UsuarioDTO()); // mock
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo usuario",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDTO.class),
                            examples = @ExampleObject(value = "{\"nombre\":\"Juan Perez\",\"email\":\"juan@correo.com\",\"password\":\"Aa123456\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "409", description = "Email ya existe")
            })
    public ResponseEntity<String> create(@RequestBody UsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado (mock)");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario existente",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"nombre\":\"Juan Actualizado\",\"email\":\"juan@correo.com\"}"))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            })
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok("Usuario actualizado (mock)");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno al eliminar usuario")
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}

