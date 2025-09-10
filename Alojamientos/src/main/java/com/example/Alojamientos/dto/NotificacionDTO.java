package com.example.Alojamientos.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {

    @NotNull
    private Integer userId;

    @NotBlank
    private String type; // NUEVA_RESERVA, CANCELACION_RESERVA, NUEVO_COMENTARIO, RESPUESTA_COMENTARIO, RECORDATORIO

    @NotBlank
    @Size(max = 150)
    private String title;

    @NotBlank
    private String message;

    private boolean read;
    private String readDate; // yyyy-MM-dd'T'HH:mm:ss
}
