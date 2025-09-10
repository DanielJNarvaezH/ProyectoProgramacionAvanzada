package com.example.Alojamientos.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlojamientoDTO {

    @NotNull
    private Integer hostId;

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    @Size(max = 255)
    private String address;

    @NotBlank
    @Size(max = 100)
    private String city;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    private Double pricePerNight;

    @NotNull
    private Integer maxCapacity;

    private String mainImage; // URL
    private boolean active;
}
