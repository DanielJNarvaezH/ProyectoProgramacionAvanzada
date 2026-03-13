package com.example.Alojamientos.businessLayer.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlojamientoDTO {

    private Integer id;  // ← necesario para navegación al detalle desde el front

    @NotNull
    private Integer hostId;

    @NotBlank(message = "El nombre del alojamiento no puede estar vacío")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "La descripción del alojamiento es obligatoria")
    private String description;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255)
    private String address;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100)
    private String city;

    // ALOJ-12: coordenadas válidas
    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0",  message = "La latitud debe ser mayor o igual a -90")
    @DecimalMax(value = "90.0",   message = "La latitud debe ser menor o igual a 90")
    private Double latitude;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
    @DecimalMax(value = "180.0",  message = "La longitud debe ser menor o igual a 180")
    private Double longitude;

    // ALOJ-12: precio > 0
    @NotNull(message = "El precio por noche es obligatorio")
    @DecimalMin(value = "0.01",   message = "El precio por noche debe ser mayor a 0")
    private Double pricePerNight;

    // ALOJ-12: capacidad >= 1
    @NotNull(message = "La capacidad máxima es obligatoria")
    @Min(value = 1,               message = "La capacidad máxima debe ser al menos 1 huésped")
    private Integer maxCapacity;

    // ALOJ-12: imagen obligatoria
    @NotBlank(message = "El alojamiento debe tener al menos una imagen principal")
    private String mainImage; // URL

    private boolean active;
}