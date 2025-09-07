package com.example.Alojamientos.controller;

import com.example.Alojamientos.dto.UsuarioRegistroDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema (huéspedes y anfitriones)")
public class UsuarioController {

    @GetMapping
    @Operation(
            summary = "Obtener todos los usuarios",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioRegistroDTO.class)
                    )
            )
    )
    public ResponseEntity<List<UsuarioRegistroDTO>> getAll() {
        return ResponseEntity.ok(List.of()); // mock vacío
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener un usuario por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no existe")
            }
    )
    public ResponseEntity<UsuarioRegistroDTO> getById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build(); // mock
    }

    @PostMapping
    @Operation(
            summary = "Registrar un nuevo usuario",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioRegistroDTO.class),
                            examples = @ExampleObject(value = "{\"name\":\"Juan Perez\",\"email\":\"juan@correo.com\",\"password\":\"Aa123456\",\"phone\":\"3123456789\",\"birthDate\":\"1995-06-10\",\"role\":\"GUEST\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario creado"),
                    @ApiResponse(responseCode = "400", description = "Validación fallida")
            }
    )
    public ResponseEntity<UsuarioRegistroDTO> create(@RequestBody UsuarioRegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dto); // mock
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar usuario",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no existe")
            }
    )
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build(); // mock
    }
}

