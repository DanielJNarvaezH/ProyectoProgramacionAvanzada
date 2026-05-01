package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "Respuesta de un anfitrión a un comentario de un huésped")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaComentarioDTO {

    @Schema(description = "ID del comentario al que se responde", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer commentId;

    @Schema(description = "ID del anfitrión que responde (debe ser dueño del alojamiento)", example = "13", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer hostId;

    @Schema(description = "Texto de la respuesta del anfitrión (máximo 500 caracteres)", example = "¡Gracias por tu reseña! Fue un placer recibirte.", maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 500)
    private String text;
}