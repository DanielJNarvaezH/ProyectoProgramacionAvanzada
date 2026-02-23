package com.example.Alojamientos.businessLayer.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String birthDate;
    private String role;
}