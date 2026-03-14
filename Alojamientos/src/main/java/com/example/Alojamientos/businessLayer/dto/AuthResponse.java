package com.example.Alojamientos.businessLayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String rol;
    private String mensaje;
    private Integer userId;  // ← ALOJ-7: id del usuario para hostId al crear alojamiento
    private String name;     // ← ALOJ-7: nombre para mostrar en navbar
}