package com.example.Alojamientos.businessLayer.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlojamientoServicioDTO {

    @NotNull
    private Integer lodgingId;

    @NotNull
    private Integer serviceId;
}
