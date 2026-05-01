package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "Código de recuperación de contraseña enviado al correo del usuario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodigoRecuperacionDTO {

    @Schema(description = "ID del usuario que solicitó la recuperación", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer userId;

    @Schema(description = "Código alfanumérico de recuperación (máximo 10 caracteres)", example = "ABC123XY90", maxLength = 10, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 10)
    private String code;

    @Schema(description = "Fecha y hora de expiración del código (ISO 8601)", example = "2026-04-30T12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String expirationDate;

    @Schema(description = "Indica si el código ya fue utilizado", example = "false", accessMode = Schema.AccessMode.READ_ONLY)
    private boolean used;
}