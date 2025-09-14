package com.example.Alojamientos.businessLayer.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagenDTO {

    @NotNull
    private Integer lodgingId;

    @NotBlank
    @Size(max = 255)
    private String url;

    @Size(max = 200)
    private String description;

    private Integer order;
}
