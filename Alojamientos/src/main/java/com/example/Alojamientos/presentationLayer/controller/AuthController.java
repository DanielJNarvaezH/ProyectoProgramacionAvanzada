package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.AuthResponse;
import com.example.Alojamientos.businessLayer.dto.LoginRequest;
import com.example.Alojamientos.businessLayer.dto.RegisterRequest;
import com.example.Alojamientos.businessLayer.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints públicos para registro, login y recuperación de contraseña.")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/register
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos."),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado.")
    })
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ya está registrado")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al registrar el usuario");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/login
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso."),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas.")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al iniciar sesión");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/recuperar-contrasena
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/recuperar-contrasena")
    @Operation(
            summary = "Solicitar código de recuperación",
            description = "Envía un código de 6 dígitos al correo del usuario. Válido por 15 minutos.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "email": "usuario@ejemplo.com" }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Código enviado exitosamente."),
            @ApiResponse(responseCode = "400", description = "El correo no está registrado.")
    })
    public ResponseEntity<?> recuperarContrasena(@RequestBody Map<String, String> body) {
        try {
            String correo = body.get("email");
            if (correo == null || correo.isBlank()) {
                return ResponseEntity.badRequest().body("El campo email es obligatorio");
            }
            String resultado = authService.solicitarRecuperacion(correo);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el código de recuperación");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/reset-contrasena
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/reset-contrasena")
    @Operation(
            summary = "Restablecer contraseña con código",
            description = "Valida el código recibido por email y actualiza la contraseña del usuario.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "email": "usuario@ejemplo.com",
                                      "codigo": "483920",
                                      "nuevaContrasena": "NuevaClave1"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente."),
            @ApiResponse(responseCode = "400", description = "Código incorrecto, expirado o contraseña inválida.")
    })
    public ResponseEntity<?> resetContrasena(@RequestBody Map<String, String> body) {
        try {
            String correo          = body.get("email");
            String codigo          = body.get("codigo");
            String nuevaContrasena = body.get("nuevaContrasena");

            if (correo == null || codigo == null || nuevaContrasena == null) {
                return ResponseEntity.badRequest().body("Los campos email, codigo y nuevaContrasena son obligatorios");
            }

            String resultado = authService.resetContrasena(correo, codigo, nuevaContrasena);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al restablecer la contraseña");
        }
    }
}