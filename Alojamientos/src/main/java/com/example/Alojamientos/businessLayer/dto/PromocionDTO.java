package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "Promoción o descuento aplicado a un alojamiento")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromocionDTO {

    @Schema(description = "ID del alojamiento al que aplica la promoción", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer lodgingId;

    @Schema(description = "Nombre de la promoción (máximo 100 caracteres)", example = "Descuento de temporada", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(description = "Descripción opcional de la promoción (máximo 255 caracteres)", example = "20% de descuento en reservas de mayo", maxLength = 255)
    @Size(max = 255)
    private String description;

    @Schema(description = "Tipo de descuento aplicado", example = "PORCENTAJE",
            allowableValues = {"PORCENTAJE", "MONTO_FIJO"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String discountType;

    @Schema(description = "Valor del descuento (porcentaje 0-100 o monto fijo en COP)", example = "20.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @DecimalMin("0.01")
    private Double discountValue;

    @Schema(description = "Fecha de inicio de la promoción (yyyy-MM-dd)", example = "2026-05-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String startDate;

    @Schema(description = "Fecha de fin de la promoción (yyyy-MM-dd)", example = "2026-05-31", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String endDate;

    @Schema(description = "Código promocional para aplicar el descuento", example = "MAYO20")
    private String promoCode;

    @Schema(description = "Indica si la promoción está activa actualmente", example = "true")
    private boolean active;
}