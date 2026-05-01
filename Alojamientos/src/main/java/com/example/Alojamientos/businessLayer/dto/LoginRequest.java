// ── LoginRequest.java ─────────────────────────────────────────────────────────
package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Credenciales para iniciar sesión")
@Data
public class LoginRequest {

    @Schema(description = "Correo electrónico del usuario", example = "juanpablo@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Contraseña del usuario", example = "MiPassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}