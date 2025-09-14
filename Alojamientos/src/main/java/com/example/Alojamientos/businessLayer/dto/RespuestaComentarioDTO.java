package com.example.Alojamientos.businessLayer.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaComentarioDTO {

    @NotNull
    private Integer commentId;

    @NotNull
    private Integer hostId;

    @NotBlank
    @Size(max = 500)
    private String text;
}
