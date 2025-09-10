package com.example.Alojamientos.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioDTO {

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 200)
    private String description;

    @Size(max = 100)
    private String icon;

    private boolean active;
}
