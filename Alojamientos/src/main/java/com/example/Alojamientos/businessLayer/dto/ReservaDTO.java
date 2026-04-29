package com.example.Alojamientos.businessLayer.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {

    private Integer id;

    @NotNull
    private Integer guestId;

    @NotNull
    private Integer lodgingId;

    @NotNull
    private String startDate;     // yyyy-MM-dd

    @NotNull
    private String endDate;       // yyyy-MM-dd

    @NotNull
    @Min(1)
    private Integer numGuests;

    @NotNull
    private Double totalPrice;

    @NotBlank
    private String status;        // PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA

    private String cancelDate;    // yyyy-MM-dd
    private String cancelReason;

    /** Fecha en que se creó la reserva — para ordenar por más reciente primero */
    private String reservationDate; // yyyy-MM-dd HH:mm
}
