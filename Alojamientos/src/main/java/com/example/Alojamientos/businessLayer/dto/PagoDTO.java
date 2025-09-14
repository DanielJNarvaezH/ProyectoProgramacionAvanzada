package com.example.Alojamientos.businessLayer.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoDTO {

    @NotNull
    private Integer reservationId;

    @NotNull
    private Double amount;

    @NotBlank
    private String method; // TARJETA_CREDITO, TARJETA_DEBITO, PAYPAL, TRANSFERENCIA

    @NotBlank
    private String status; // PENDIENTE, COMPLETADO, FALLIDO, REEMBOLSADO

    private String externalRef;
}
