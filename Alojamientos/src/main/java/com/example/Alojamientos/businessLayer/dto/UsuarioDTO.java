// ── UsuarioDTO.java ──────────────────────────────────────────────────────────
package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "Datos de un usuario de la plataforma")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    @Schema(description = "ID único del usuario", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "Nombre completo del usuario", example = "Juan Pablo García", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(description = "Correo electrónico único del usuario", example = "juanpablo@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Email
    private String email;

    @Schema(description = "Contraseña (mínimo 8 caracteres)", example = "MiPassword123", minLength = 8, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 8)
    private String password;

    @Schema(description = "Número de teléfono (exactamente 10 dígitos)", example = "3001234567", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "phone must be 10 digits")
    private String phone;

    @Schema(description = "Fecha de nacimiento en formato yyyy-MM-dd", example = "1995-06-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String birthDate;

    @Schema(description = "Rol del usuario en la plataforma", example = "USUARIO",
            allowableValues = {"USUARIO", "ANFITRION", "ADMIN"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String role;
}
