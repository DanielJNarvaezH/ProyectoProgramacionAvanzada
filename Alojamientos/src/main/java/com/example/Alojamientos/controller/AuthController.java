package com.example.Alojamientos.controller;

import com.example.Alojamientos.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, login y recuperación de contraseña")
public class AuthController {

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario (huésped o anfitrión)",
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
                    @ApiResponse(responseCode = "400", description = "Validación fallida"),
                    @ApiResponse(responseCode = "409", description = "Email ya existe")
            }
    )
    public ResponseEntity<String> register(@RequestBody UsuarioRegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado (mock)");
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"email\":\"juan@correo.com\",\"password\":\"Aa123456\"}")
                    )),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login OK",
                            content = @Content(schema = @Schema(implementation = AuthResponseDTO.class),
                                    examples = @ExampleObject(value = "{\"token\":\"eyJ...\",\"expiresIn\":86400}"))),
                    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
            }
    )
    public ResponseEntity<AuthResponseDTO> login(@RequestBody Map<String,String> body) {
        AuthResponseDTO resp = new AuthResponseDTO();
        resp.setToken("MOCK_TOKEN");
        resp.setExpiresIn(86400);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar código de recuperación (envía código por email)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"email\":\"juan@correo.com\"}")
                    )),
            responses = {@ApiResponse(responseCode = "200", description = "Si email existe, se envía código")}
    )
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String,String> body) {
        return ResponseEntity.ok("Si existe el email, se envió un código (mock)");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña con código",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"email\":\"juan@correo.com\",\"code\":\"123456\",\"newPassword\":\"Aa123456\"}")
                    )),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contraseña cambiada"),
                    @ApiResponse(responseCode = "400", description = "Código inválido o expirado")
            }
    )
    public ResponseEntity<String> resetPassword(@RequestBody Map<String,String> body) {
        return ResponseEntity.ok("Contraseña actualizada (mock)");
    }
}


