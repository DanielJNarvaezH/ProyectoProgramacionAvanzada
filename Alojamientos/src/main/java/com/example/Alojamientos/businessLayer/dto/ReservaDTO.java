package com.example.Alojamientos.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "Datos de una reserva de alojamiento")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {

    @Schema(description = "ID único de la reserva (generado por el servidor)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "ID del huésped que realiza la reserva", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer guestId;

    @Schema(description = "ID del alojamiento a reservar", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer lodgingId;

    @Schema(description = "Fecha de check-in en formato yyyy-MM-dd", example = "2026-05-10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String startDate;

    @Schema(description = "Fecha de check-out en formato yyyy-MM-dd", example = "2026-05-13", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String endDate;

    @Schema(description = "Número de huéspedes (mínimo 1)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Min(1)
    private Integer numGuests;

    @Schema(description = "Precio total de la reserva en pesos colombianos", example = "600000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Double totalPrice;

    @Schema(description = "Estado de la reserva", example = "CONFIRMADA",
            allowableValues = {"PENDIENTE", "CONFIRMADA", "CANCELADA", "COMPLETADA"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String status;

    @Schema(description = "Fecha y hora de cancelación (solo si status=CANCELADA)", example = "2026-04-30T22:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private String cancelDate;

    @Schema(description = "Motivo de cancelación (solo si status=CANCELADA)", example = "Cambio de planes", accessMode = Schema.AccessMode.READ_ONLY)
    private String cancelReason;

    @Schema(description = "Fecha y hora en que se creó la reserva", example = "2026-04-28T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private String reservationDate;
}
