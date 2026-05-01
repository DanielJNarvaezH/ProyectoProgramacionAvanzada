package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "Servicio o amenidad disponible en la plataforma (WiFi, Cocina, Parqueadero, etc.)")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioDTO {

    @Schema(description = "ID único del servicio (generado por el servidor)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "Nombre del servicio (máximo 50 caracteres)", example = "WiFi", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 50)
    private String name;

    @Schema(description = "Descripción del servicio (máximo 200 caracteres)", example = "Conexión WiFi de alta velocidad", maxLength = 200)
    @Size(max = 200)
    private String description;

    @Schema(description = "Clase CSS o nombre del ícono de Font Awesome para el servicio", example = "fa-wifi", maxLength = 100)
    @Size(max = 100)
    private String icon;

    @Schema(description = "Indica si el servicio está activo y disponible para asignar", example = "true")
    private boolean active;
}
