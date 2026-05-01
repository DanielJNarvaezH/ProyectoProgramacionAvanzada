package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "Datos de un comentario y calificación post-estadia")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioDTO {

    @Schema(description = "ID único del comentario (generado por el servidor)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "ID de la reserva COMPLETADA asociada al comentario", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer reservationId;

    @Schema(description = "ID del usuario huésped que comenta", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer userId;

    @Schema(description = "Calificación del alojamiento de 1 a 5 estrellas", example = "4", minimum = "1", maximum = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Schema(description = "Texto del comentario (máximo 500 caracteres)", example = "Excelente alojamiento, muy limpio y bien ubicado.", maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 500)
    private String text;
}