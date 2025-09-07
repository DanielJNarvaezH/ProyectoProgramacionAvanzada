package com.example.Alojamientos.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ComentarioCreateDTO {
    @NotNull
    private Long reservationId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 500)
    private String text;
}
