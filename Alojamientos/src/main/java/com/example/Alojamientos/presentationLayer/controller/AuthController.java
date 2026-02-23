package com.example.Alojamientos.presentationLayer.controller;

import com.example.Alojamientos.businessLayer.dto.AuthResponse;
import com.example.Alojamientos.businessLayer.dto.LoginRequest;
import com.example.Alojamientos.businessLayer.dto.RegisterRequest;
import com.example.Alojamientos.businessLayer.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Autenticación",
        description = "Endpoints públicos para registro e inicio de sesión en la plataforma."
)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/register
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(
            summary = "Registrar un nuevo usuario",
            description = """
                    Crea una nueva cuenta y devuelve un token JWT listo para usar.
                    
                    **Reglas de negocio:**
                    - El correo electrónico debe ser único en el sistema.
                    - La contraseña debe tener mínimo 8 caracteres, al menos una mayúscula y un número.
                    - Los anfitriones (ANFITRION) deben ser mayores de 18 años.
                    - Roles permitidos: `USUARIO`, `ANFITRION`.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Usuario normal",
                                            summary = "Registro como usuario",
                                            value = """
                                                    {
                                                      "name": "Juan Pérez",
                                                      "email": "juan@example.com",
                                                      "password": "Abc12345",
                                                      "phone": "3001234567",
                                                      "birthDate": "2000-05-15",
                                                      "role": "USUARIO"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Anfitrión",
                                            summary = "Registro como anfitrión",
                                            value = """
                                                    {
                                                      "name": "María García",
                                                      "email": "maria@example.com",
                                                      "password": "Xyz98765",
                                                      "phone": "3109876543",
                                                      "birthDate": "1990-03-20",
                                                      "role": "ANFITRION"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos.",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado.",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = "text/plain"))
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
    @Operation(
            summary = "Iniciar sesión",
            description = """
                    Autentica las credenciales del usuario y devuelve un token JWT válido por 24h.
                    
                    **Uso del token:**
                    Incluye el token en el header de las peticiones protegidas:
                    `Authorization: Bearer <token>`
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de login",
                                    value = """
                                            {
                                              "email": "juan@example.com",
                                              "password": "Abc12345"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o cuenta desactivada.",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = "text/plain"))
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
}