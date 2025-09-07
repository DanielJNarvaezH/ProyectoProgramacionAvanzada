package com.example.Alojamientos.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ReservaCreateDTO {
    @NotNull
    private Long accommodationId;

    @NotBlank
    private String checkIn; // yyyy-MM-dd

    @NotBlank
    private String checkOut;

    @NotNull
    @Min(1)
    private Integer guests;

    private String couponCode;
}
