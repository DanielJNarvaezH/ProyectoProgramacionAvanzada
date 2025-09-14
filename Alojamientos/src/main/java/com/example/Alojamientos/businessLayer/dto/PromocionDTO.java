package com.example.Alojamientos.businessLayer.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromocionDTO {

    @NotNull
    private Integer lodgingId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    @NotBlank
    private String discountType; // PORCENTAJE, MONTO_FIJO

    @NotNull
    @DecimalMin("0.01")
    private Double discountValue;

    @NotNull
    private String startDate; // yyyy-MM-dd

    @NotNull
    private String endDate; // yyyy-MM-dd

    private String promoCode;
    private boolean active;
}
