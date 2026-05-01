package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "Imagen asociada a un alojamiento")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagenDTO {

    @Schema(description = "ID único de la imagen (generado por el servidor)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "ID del alojamiento al que pertenece la imagen", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer lodgingId;

    @Schema(description = "URL de la imagen en Cloudinary", example = "https://res.cloudinary.com/hosped/image/upload/v1/foto1.jpg", maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 255)
    private String url;

    @Schema(description = "Descripción alternativa de la imagen (máximo 200 caracteres)", example = "Vista exterior de la cabaña", maxLength = 200)
    @Size(max = 200)
    private String description;

    @Schema(description = "Orden de visualización de la imagen en la galería", example = "1")
    private Integer order;
}