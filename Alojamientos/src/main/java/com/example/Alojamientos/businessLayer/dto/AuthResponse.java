package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "Respuesta devuelta al autenticarse correctamente")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    @Schema(description = "Token JWT para autenticar peticiones posteriores", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Correo electrónico del usuario autenticado", example = "juanpablo@gmail.com")
    private String email;

    @Schema(description = "Rol del usuario", example = "USUARIO", allowableValues = {"USUARIO", "ANFITRION", "ADMIN"})
    private String rol;

    @Schema(description = "Mensaje descriptivo de la operación", example = "Inicio de sesión exitoso")
    private String mensaje;

    @Schema(description = "ID único del usuario autenticado", example = "10")
    private Integer userId;

    @Schema(description = "Nombre completo del usuario autenticado", example = "Juan Pablo García")
    private String name;
}