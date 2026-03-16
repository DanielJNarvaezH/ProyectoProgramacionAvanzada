package com.example.Alojamientos.businessLayer.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagenDTO {

    // FIX ALOJ-11 y 8: id necesario para que el front pueda identificar
    // qué imágenes ya estaban en BD al editar un alojamiento
    private Integer id;

    @NotNull
    private Integer lodgingId;

    @NotBlank
    @Size(max = 255)
    private String url;

    @Size(max = 200)
    private String description;

    private Integer order;
}