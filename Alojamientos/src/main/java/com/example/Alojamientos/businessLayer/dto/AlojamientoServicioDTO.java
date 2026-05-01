package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "Relación entre un alojamiento y un servicio disponible")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlojamientoServicioDTO {

    @Schema(description = "ID del alojamiento al que se asocia el servicio", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer lodgingId;

    @Schema(description = "ID del servicio a asociar al alojamiento", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer serviceId;

    @Schema(description = "Nombre del servicio (solo lectura, enriquecido en respuesta)", example = "WiFi", accessMode = Schema.AccessMode.READ_ONLY)
    private String serviceName;

    @Schema(description = "Ícono del servicio (solo lectura, enriquecido en respuesta)", example = "fa-wifi", accessMode = Schema.AccessMode.READ_ONLY)
    private String serviceIcon;
}