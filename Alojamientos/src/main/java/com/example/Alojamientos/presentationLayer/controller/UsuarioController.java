package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.UsuarioDTO;
import com.example.Alojamientos.businessLayer.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ============================================================
    // AUTH-21: Obtener perfil del usuario autenticado
    // ============================================================
    @GetMapping("/me")
    @Operation(
            summary = "Obtener perfil del usuario autenticado",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Perfil obtenido correctamente",
                            content = @Content(schema = @Schema(implementation = UsuarioDTO.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            }
    )
    public ResponseEntity<?> getMe() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName(); // el subject del JWT es el correo
            UsuarioDTO usuario = usuarioService.buscarPorEmail(email);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }
    }

    // ============================================================
    // AUTH-21: Actualizar perfil del usuario autenticado
    // ============================================================
    @PutMapping("/me")
    @Operation(
            summary = "Actualizar perfil del usuario autenticado",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos")
            }
    )
    public ResponseEntity<?> updateMe(@RequestBody UsuarioDTO dto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            UsuarioDTO usuarioActual = usuarioService.buscarPorEmail(email);
            UsuarioDTO actualizado = usuarioService.actualizarUsuario(usuarioActual.getId(), dto);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Datos inválidos");
        }
    }

    // ============================================================
    // Listar todos los usuarios activos
    // ============================================================
    @GetMapping
    @Operation(summary = "Listar todos los usuarios activos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida correctamente",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioDTO.class)))),
                    @ApiResponse(responseCode = "404", description = "No hay usuarios registrados"),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
            })
    public ResponseEntity<?> getAll() {
        try {
            List<UsuarioDTO> lista = usuarioService.listarTodos();
            if (lista.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No hay usuarios registrados");
            }
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Solicitud inválida");
        }
    }

    // ============================================================
    // Obtener usuario por ID
    // ============================================================
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                            content = @Content(schema = @Schema(implementation = UsuarioDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "ID inválido")
            })
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            UsuarioDTO usuario = usuarioService.obtenerPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ID inválido");
        }
    }

    // ============================================================
    // Crear nuevo usuario
    // ============================================================
    @PostMapping
    @Operation(summary = "Crear un nuevo usuario",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "409", description = "Email o teléfono ya existe")
            })
    public ResponseEntity<?> create(@RequestBody UsuarioDTO dto) {
        try {
            UsuarioDTO creado = usuarioService.crearUsuario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg.contains("registrado")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(msg);
            }
            return ResponseEntity.badRequest().body(msg);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear usuario");
        }
    }

    // ============================================================
    // Actualizar usuario
    // ============================================================
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario existente",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos")
            })
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody UsuarioDTO dto) {
        try {
            UsuarioDTO actualizado = usuarioService.actualizarUsuario(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Datos inválidos");
        }
    }

    // ============================================================
    // Cambiar contraseña
    // ============================================================
    @PutMapping("/{id}/cambiar-contrasena")
    @Operation(summary = "Cambiar contraseña del usuario",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"oldPassword\":\"Aa123456\",\"newPassword\":\"Bb123456\"}"))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Contraseña inválida")
            })
    public ResponseEntity<?> changePassword(@PathVariable Integer id, @RequestBody UsuarioDTO dto) {
        try {
            usuarioService.cambiarContrasena(id, dto.getPassword(), dto.getPassword());
            return ResponseEntity.ok("Contraseña actualizada correctamente");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg.contains("Usuario no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
            }
            return ResponseEntity.badRequest().body(msg);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cambiar contraseña");
        }
    }

    // ============================================================
    // Eliminar usuario
    // ============================================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "No se puede eliminar usuario por restricciones")
            })
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg.contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
            }
            return ResponseEntity.badRequest().body(msg);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar usuario");
        }
    }

    // ============================================================
    // Buscar usuario por email
    // ============================================================
    @GetMapping("/email/{email}")
    @Operation(summary = "Buscar usuario por email",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                            content = @Content(schema = @Schema(implementation = UsuarioDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Email inválido")
            })
    public ResponseEntity<?> getByEmail(@PathVariable String email) {
        try {
            UsuarioDTO usuario = usuarioService.buscarPorEmail(email);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Email inválido");
        }
    }

    // ============================================================
    // Verificar si email ya existe
    // ============================================================
    @GetMapping("/existe/{email}")
    @Operation(summary = "Verificar si un email ya está registrado",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Consulta realizada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Email inválido"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public ResponseEntity<?> emailExists(@PathVariable String email) {
        try {
            if (email == null || email.isBlank() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return ResponseEntity.badRequest().body("Email inválido");
            }

            boolean existe = usuarioService.existeEmail(email);
            return ResponseEntity.ok(existe);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al verificar email");
        }
    }

    // ============================================================
    // Verificar si usuario está activo (soft delete / sin alojamientos activos)
    // ============================================================
    @GetMapping("/{id}/activo")
    @Operation(summary = "Verificar si un usuario está activo y puede ser eliminado",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario puede ser eliminado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Usuario no puede ser eliminado (tiene alojamientos activos o reservas futuras)")
            })
    public ResponseEntity<?> isActive(@PathVariable Integer id) {
        try {
            UsuarioDTO usuario = usuarioService.obtenerPorId(id);

            try {
                usuarioService.eliminarUsuario(id);
                return ResponseEntity.ok("Usuario puede ser eliminado");
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ID inválido");
        }
    }

}