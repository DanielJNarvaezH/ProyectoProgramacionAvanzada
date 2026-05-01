// ── NotificacionDTO.java ──────────────────────────────────────────────────────
package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "Notificación enviada a un usuario de la plataforma")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {

    @Schema(description = "ID único de la notificación", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "ID del usuario destinatario de la notificación", example = "13", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer userId;

    @Schema(description = "Tipo de notificación",
            example = "NUEVA_RESERVA",
            allowableValues = {"NUEVA_RESERVA", "CANCELACION", "PROMOCION", "MENSAJE", "OTRO"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String type;

    @Schema(description = "Título corto de la notificación (máximo 150 caracteres)", example = "Nueva reserva recibida", maxLength = 150, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 150)
    private String title;

    @Schema(description = "Cuerpo completo del mensaje de la notificación", example = "Tienes una nueva reserva en Cabaña del Quindío del 2026-05-10 al 2026-05-13", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String message;

    @Schema(description = "Indica si la notificación ya fue leída por el usuario", example = "false")
    private boolean read;

    @Schema(description = "Fecha y hora en que se leyó la notificación (ISO 8601)", example = "2026-04-30T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private String readDate;
}
