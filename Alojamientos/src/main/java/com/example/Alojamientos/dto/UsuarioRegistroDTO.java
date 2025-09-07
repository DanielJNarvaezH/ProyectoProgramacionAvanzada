package com.example.Alojamientos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRegistroDTO {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "phone must be 10 digits")
    private String phone;

    @NotNull
    private String birthDate; // ISO date yyyy-MM-dd

    @NotBlank
    private String role; // GUEST or HOST
}

