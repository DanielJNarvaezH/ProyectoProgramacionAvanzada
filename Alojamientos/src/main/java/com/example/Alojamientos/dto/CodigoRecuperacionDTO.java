package com.example.Alojamientos.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodigoRecuperacionDTO {

    @NotNull
    private Integer userId;

    @NotBlank
    @Size(max = 10)
    private String code;

    @NotNull
    private String expirationDate; // ISO timestamp yyyy-MM-dd'T'HH:mm:ss

    private boolean used;
}
