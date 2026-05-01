package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "Datos de un pago asociado a una reserva")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoDTO {

    @Schema(description = "ID de la reserva a la que pertenece el pago", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer reservationId;

    @Schema(description = "Monto total del pago en pesos colombianos", example = "600000.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Double amount;

    @Schema(description = "Método de pago utilizado", example = "TARJETA_CREDITO",
            allowableValues = {"TARJETA_CREDITO", "TARJETA_DEBITO", "PAYPAL", "TRANSFERENCIA"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String method;

    @Schema(description = "Estado actual del pago", example = "COMPLETADO",
            allowableValues = {"PENDIENTE", "COMPLETADO", "FALLIDO", "REEMBOLSADO"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String status;

    @Schema(description = "Referencia externa del proveedor de pago (ID de transacción)", example = "TXN-2026-00123", accessMode = Schema.AccessMode.READ_ONLY)
    private String externalRef;
}