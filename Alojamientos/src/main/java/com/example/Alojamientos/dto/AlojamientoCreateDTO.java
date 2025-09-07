package com.example.Alojamientos.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class AlojamientoCreateDTO {
    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotBlank
    private String city;

    @NotBlank
    private String address;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    private Double pricePerNight;

    @NotNull
    private Integer capacity;

    private List<String> services;

    @NotNull
    @Size(min = 1, max = 10)
    private List<String> images; // URLs or upload tokens
}
