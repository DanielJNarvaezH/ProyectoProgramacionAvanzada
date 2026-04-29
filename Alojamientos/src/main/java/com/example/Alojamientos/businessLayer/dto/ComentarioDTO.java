package com.example.Alojamientos.businessLayer.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioDTO {

    /** ID del comentario (PK) — necesario para que el frontend
     *  pueda pasar el ID correcto al crear respuestas (COMENT-6). */
    private Integer id;

    @NotNull
    private Integer reservationId;

    @NotNull
    private Integer userId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotBlank
    @Size(max = 500)
    private String text;
}