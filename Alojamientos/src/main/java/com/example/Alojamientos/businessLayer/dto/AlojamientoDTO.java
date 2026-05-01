package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "Datos de un alojamiento publicado en la plataforma")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlojamientoDTO {

    @Schema(description = "ID único del alojamiento (generado por el servidor)", example = "3", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "ID del anfitrión dueño del alojamiento", example = "13", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer hostId;

    @Schema(description = "Nombre del alojamiento (máximo 150 caracteres)", example = "Cabaña de verano en el Quindío", maxLength = 150, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre del alojamiento no puede estar vacío")
    @Size(max = 150)
    private String name;

    @Schema(description = "Descripción detallada del alojamiento", example = "Hermosa cabaña rodeada de naturaleza...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La descripción del alojamiento es obligatoria")
    private String description;

    @Schema(description = "Dirección completa del alojamiento", example = "Calle 10 #5-30, Armenia", maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255)
    private String address;

    @Schema(description = "Ciudad donde se ubica el alojamiento", example = "Armenia", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100)
    private String city;

    @Schema(description = "Latitud geográfica del alojamiento (-90 a 90)", example = "4.5339", minimum = "-90", maximum = "90", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0",  message = "La latitud debe ser mayor o igual a -90")
    @DecimalMax(value = "90.0",   message = "La latitud debe ser menor o igual a 90")
    private Double latitude;

    @Schema(description = "Longitud geográfica del alojamiento (-180 a 180)", example = "-75.6820", minimum = "-180", maximum = "180", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
    @DecimalMax(value = "180.0",  message = "La longitud debe ser menor o igual a 180")
    private Double longitude;

    @Schema(description = "Precio por noche en pesos colombianos (mayor a 0)", example = "200000.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El precio por noche es obligatorio")
    @DecimalMin(value = "0.01",   message = "El precio por noche debe ser mayor a 0")
    private Double pricePerNight;

    @Schema(description = "Capacidad máxima de huéspedes (mínimo 1)", example = "4", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La capacidad máxima es obligatoria")
    @Min(value = 1, message = "La capacidad máxima debe ser al menos 1 huésped")
    private Integer maxCapacity;

    @Schema(description = "URL de la imagen principal del alojamiento", example = "https://res.cloudinary.com/hosped/image/upload/v1/main.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El alojamiento debe tener al menos una imagen principal")
    private String mainImage;

    @Schema(description = "Indica si el alojamiento está activo y visible en el catálogo", example = "true")
    private boolean active;

    @Schema(description = "Indica si el alojamiento fue eliminado (soft delete)", example = "false")
    private boolean deleted;
}