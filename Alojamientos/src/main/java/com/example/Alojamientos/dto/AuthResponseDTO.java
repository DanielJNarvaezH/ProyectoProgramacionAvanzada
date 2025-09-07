package com.example.Alojamientos.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private long expiresIn;
}
