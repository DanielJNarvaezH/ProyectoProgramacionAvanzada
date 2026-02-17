package com.example.Alojamientos.businessLayer.dto.auth;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;      // "Bearer"
    private Long expiresIn;        // segundos hasta expiración del accessToken

    private Integer userId;
    private String name;
    private String email;
    private String role;
}