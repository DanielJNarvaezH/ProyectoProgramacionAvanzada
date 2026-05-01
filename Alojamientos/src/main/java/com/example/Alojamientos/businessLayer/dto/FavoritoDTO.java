package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "Relación de un alojamiento marcado como favorito por un huésped")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoritoDTO {

    @Schema(description = "ID del usuario huésped que marcó el favorito", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer userId;

    @Schema(description = "ID del alojamiento marcado como favorito", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer lodgingId;
}