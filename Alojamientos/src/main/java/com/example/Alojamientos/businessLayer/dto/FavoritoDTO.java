package com.example.Alojamientos.businessLayer.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoritoDTO {

    @NotNull
    private Integer userId;

    @NotNull
    private Integer lodgingId;
}
