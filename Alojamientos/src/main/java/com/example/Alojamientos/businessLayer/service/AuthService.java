package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.AuthResponse;
import com.example.Alojamientos.businessLayer.dto.LoginRequest;
import com.example.Alojamientos.businessLayer.dto.RegisterRequest;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import com.example.Alojamientos.persistenceLayer.repository.UsuarioRepository;
import com.example.Alojamientos.securityLayer.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByCorreo(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        validarContrasena(request.getPassword());

        LocalDate fechaNacimiento = LocalDate.parse(request.getBirthDate());

        if ("ANFITRION".equalsIgnoreCase(request.getRole())) {
            int edad = java.time.Period.between(fechaNacimiento, LocalDate.now()).getYears();
            if (edad < 18) {
                throw new IllegalArgumentException("Debes ser mayor de 18 años para ser anfitrión");
            }
        }

        UsuarioEntity.Rol rol;
        try {
            rol = UsuarioEntity.Rol.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol inválido. Valores permitidos: USUARIO, ANFITRION");
        }

        UsuarioEntity usuario = UsuarioEntity.builder()
                .nombre(request.getName())
                .correo(request.getEmail())
                .contrasena(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getPhone())
                .fechaNacimiento(fechaNacimiento)
                .rol(rol)
                .activo(true)
                .build();

        UsuarioEntity guardado = usuarioRepository.save(usuario);

        String token = jwtService.generarTokenConRol(guardado.getCorreo(), guardado.getRol().name());

        return AuthResponse.builder()
                .token(token)
                .email(guardado.getCorreo())
                .rol(guardado.getRol().name())
                .mensaje("Registro exitoso")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UsuarioEntity usuario = usuarioRepository.findByCorreo(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("La cuenta está desactivada");
        }

        String token = jwtService.generarTokenConRol(usuario.getCorreo(), usuario.getRol().name());

        return AuthResponse.builder()
                .token(token)
                .email(usuario.getCorreo())
                .rol(usuario.getRol().name())
                .mensaje("Login exitoso")
                .build();
    }

    private void validarContrasena(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos una letra mayúscula");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos un número");
        }
    }
}