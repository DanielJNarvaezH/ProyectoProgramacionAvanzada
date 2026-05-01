package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Datos necesarios para registrar un nuevo usuario")
@Data
public class RegisterRequest {

    @Schema(description = "Nombre completo del usuario", example = "Juan Pablo García", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Correo electrónico único", example = "juanpablo@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Número de teléfono (10 dígitos)", example = "3001234567", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "Contraseña (mínimo 8 caracteres)", example = "MiPassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "Fecha de nacimiento en formato yyyy-MM-dd", example = "1995-06-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private String birthDate;

    @Schema(description = "Rol del usuario", example = "USUARIO",
            allowableValues = {"USUARIO", "ANFITRION"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;
}