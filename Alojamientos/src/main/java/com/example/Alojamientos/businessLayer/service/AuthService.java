package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.auth.*;
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

        String accessToken  = jwtService.generarTokenConRol(guardado.getCorreo(), guardado.getRol().name());
        String refreshToken = jwtService.generarRefreshToken(guardado.getCorreo());

        return buildAuthResponse(guardado, accessToken, refreshToken);
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

        String accessToken  = jwtService.generarTokenConRol(usuario.getCorreo(), usuario.getRol().name());
        String refreshToken = jwtService.generarRefreshToken(usuario.getCorreo());

        return buildAuthResponse(usuario, accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!jwtService.esTokenEstructuralmenteValido(token)) {
            throw new IllegalArgumentException("Refresh token inválido");
        }

        if (jwtService.estaExpirado(token)) {
            throw new IllegalArgumentException("El refresh token ha expirado. Inicia sesión nuevamente");
        }

        String email = jwtService.extraerEmail(token);

        UsuarioEntity usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("La cuenta está desactivada");
        }

        String nuevoAccessToken = jwtService.generarTokenConRol(usuario.getCorreo(), usuario.getRol().name());

        return buildAuthResponse(usuario, nuevoAccessToken, token);
    }

    private AuthResponse buildAuthResponse(UsuarioEntity usuario, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.extraerExpiracion(accessToken).getTime() - System.currentTimeMillis())
                .userId(usuario.getId())
                .name(usuario.getNombre())
                .email(usuario.getCorreo())
                .role(usuario.getRol().name())
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