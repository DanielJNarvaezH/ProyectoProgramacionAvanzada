package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.AuthResponse;
import com.example.Alojamientos.businessLayer.dto.LoginRequest;
import com.example.Alojamientos.businessLayer.dto.RegisterRequest;
import com.example.Alojamientos.persistenceLayer.entity.CodigoRecuperacionEntity;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import com.example.Alojamientos.persistenceLayer.repository.CodigoRecuperacionRepository;
import com.example.Alojamientos.persistenceLayer.repository.UsuarioRepository;
import com.example.Alojamientos.securityLayer.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UsuarioRepository            usuarioRepository;
    private final PasswordEncoder              passwordEncoder;
    private final JwtService                   jwtService;
    private final AuthenticationManager        authenticationManager;
    private final CodigoRecuperacionRepository codigoRepository;
    private final EmailService                 emailService;

    // ─────────────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────
    // RECUPERAR CONTRASEÑA — Paso 1: enviar código
    // ─────────────────────────────────────────────────────────────

// Reemplaza el método solicitarRecuperacion en AuthService.java

    @Transactional
    public String solicitarRecuperacion(String correo) {
        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("No existe una cuenta con ese correo"));

        codigoRepository.deleteAllByCorreo(correo);

        String codigo = String.format("%06d", new Random().nextInt(999999));

        CodigoRecuperacionEntity entidad = CodigoRecuperacionEntity.builder()
                .correo(correo)
                .usuario(usuario)
                .codigo(codigo)
                .fechaCreacion(Timestamp.valueOf(LocalDateTime.now()))
                .fechaExpiracion(Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)))
                .usado(false)
                .build();

        codigoRepository.save(entidad);
        codigoRepository.flush(); // ← fuerza el insert ANTES de enviar el email

        try {
            emailService.enviarCodigoRecuperacion(correo, codigo);
        } catch (Exception e) {
            // Si falla el email, igual retornamos éxito — el código está guardado
            // El usuario puede solicitarlo de nuevo
            throw new RuntimeException("El código fue generado pero no se pudo enviar el email. Verifica la configuración de correo.");
        }

        return "Código de recuperación enviado a " + correo;
    }

    // ─────────────────────────────────────────────────────────────
    // RECUPERAR CONTRASEÑA — Paso 2: validar código y cambiar
    // ─────────────────────────────────────────────────────────────

    public String resetContrasena(String correo, String codigo, String nuevaContrasena) {
        CodigoRecuperacionEntity entidad = codigoRepository
                .findTopByCorreoAndUsadoFalseOrderByFechaExpiracionDesc(correo)
                .orElseThrow(() -> new IllegalArgumentException("No hay un código de recuperación activo para este correo"));

        if (entidad.estaExpirado()) {
            throw new IllegalArgumentException("El código ha expirado. Solicita uno nuevo.");
        }
        if (!entidad.getCodigo().equals(codigo)) {
            throw new IllegalArgumentException("El código ingresado es incorrecto");
        }

        validarContrasena(nuevaContrasena);

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);

        entidad.setUsado(true);
        codigoRepository.save(entidad);

        return "Contraseña restablecida exitosamente";
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDACIONES PRIVADAS
    // ─────────────────────────────────────────────────────────────

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