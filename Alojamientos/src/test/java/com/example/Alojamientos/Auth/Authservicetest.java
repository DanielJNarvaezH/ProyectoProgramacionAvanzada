package com.example.Alojamientos.Auth;

import com.example.Alojamientos.businessLayer.dto.AuthResponse;
import com.example.Alojamientos.businessLayer.dto.LoginRequest;
import com.example.Alojamientos.businessLayer.dto.RegisterRequest;
import com.example.Alojamientos.businessLayer.service.AuthService;
import com.example.Alojamientos.businessLayer.service.EmailService;
import com.example.Alojamientos.persistenceLayer.entity.CodigoRecuperacionEntity;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import com.example.Alojamientos.persistenceLayer.repository.CodigoRecuperacionRepository;
import com.example.Alojamientos.persistenceLayer.repository.UsuarioRepository;
import com.example.Alojamientos.securityLayer.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de AuthService")
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CodigoRecuperacionRepository codigoRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private UsuarioEntity usuarioEntity;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Juan Pérez");
        registerRequest.setEmail("juan@correo.com");
        registerRequest.setPassword("Password1");
        registerRequest.setPhone("3001234567");
        registerRequest.setBirthDate("1995-06-15");
        registerRequest.setRole("USUARIO");

        usuarioEntity = UsuarioEntity.builder()
                .id(1)
                .nombre("Juan Pérez")
                .correo("juan@correo.com")
                .contrasena("encoded")
                .rol(UsuarioEntity.Rol.USUARIO)
                .activo(true)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register - exitoso con rol USUARIO")
    void register_exitoso() {
        when(usuarioRepository.existsByCorreo("juan@correo.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(usuarioRepository.save(any())).thenReturn(usuarioEntity);
        when(jwtService.generarTokenConRol("juan@correo.com", "USUARIO")).thenReturn("token123");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getEmail()).isEqualTo("juan@correo.com");
        verify(usuarioRepository).save(any());
    }

    @Test
    @DisplayName("register - error: email ya registrado")
    void register_emailYaRegistrado() {
        when(usuarioRepository.existsByCorreo("juan@correo.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está registrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("register - error: contraseña sin mayúscula")
    void register_contrasenaSinMayuscula() {
        registerRequest.setPassword("password1");

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayúscula");
    }

    @Test
    @DisplayName("register - error: contraseña sin número")
    void register_contrasenaSinNumero() {
        registerRequest.setPassword("Password");

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("número");
    }

    @Test
    @DisplayName("register - error: contraseña muy corta")
    void register_contrasenaMuyCorta() {
        registerRequest.setPassword("Pa1");

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8 caracteres");
    }

    @Test
    @DisplayName("register - error: rol inválido")
    void register_rolInvalido() {
        when(usuarioRepository.existsByCorreo(any())).thenReturn(false);
        registerRequest.setRole("SUPERADMIN");

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rol inválido");
    }

    @Test
    @DisplayName("register - error: anfitrión menor de 18 años")
    void register_anfitrionMenorDeEdad() {
        when(usuarioRepository.existsByCorreo(any())).thenReturn(false);
        registerRequest.setRole("ANFITRION");
        registerRequest.setBirthDate("2015-01-01");

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor de 18");
    }

    @Test
    @DisplayName("register - exitoso con rol ANFITRION mayor de edad")
    void register_anfitrionMayorDeEdad() {
        when(usuarioRepository.existsByCorreo(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        UsuarioEntity anfitrion = UsuarioEntity.builder()
                .id(2).correo("host@correo.com")
                .rol(UsuarioEntity.Rol.ANFITRION).activo(true).build();
        when(usuarioRepository.save(any())).thenReturn(anfitrion);
        when(jwtService.generarTokenConRol(any(), any())).thenReturn("tokenHost");

        registerRequest.setRole("ANFITRION");
        registerRequest.setBirthDate("1990-01-01");
        registerRequest.setEmail("host@correo.com");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("tokenHost");
    }

    // ─────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login - exitoso")
    void login_exitoso() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("juan@correo.com");
        loginRequest.setPassword("Password1");

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("juan@correo.com", "Password1"));
        when(usuarioRepository.findByCorreo("juan@correo.com")).thenReturn(Optional.of(usuarioEntity));
        when(jwtService.generarTokenConRol("juan@correo.com", "USUARIO")).thenReturn("token123");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getMensaje()).isEqualTo("Login exitoso");
    }

    @Test
    @DisplayName("login - error: cuenta desactivada")
    void login_cuentaDesactivada() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("juan@correo.com");
        loginRequest.setPassword("Password1");

        usuarioEntity.setActivo(false);
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("juan@correo.com", "Password1"));
        when(usuarioRepository.findByCorreo("juan@correo.com")).thenReturn(Optional.of(usuarioEntity));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desactivada");
    }

    // ─────────────────────────────────────────────────────────────
    // RESET CONTRASEÑA
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("resetContrasena - exitoso")
    void resetContrasena_exitoso() {
        CodigoRecuperacionEntity codigo = new CodigoRecuperacionEntity();
        codigo.setCodigo("123456");
        codigo.setFechaExpiracion(Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)));
        codigo.setUsado(false);

        when(codigoRepository.findTopByCorreoAndUsadoFalseOrderByFechaExpiracionDesc("juan@correo.com"))
                .thenReturn(Optional.of(codigo));
        when(usuarioRepository.findByCorreo("juan@correo.com")).thenReturn(Optional.of(usuarioEntity));
        when(passwordEncoder.encode("NuevaClave1")).thenReturn("encodedNueva");

        String resultado = authService.resetContrasena("juan@correo.com", "123456", "NuevaClave1");

        assertThat(resultado).contains("exitosamente");
        assertThat(codigo.isUsado()).isTrue();
        verify(usuarioRepository).save(usuarioEntity);
    }

    @Test
    @DisplayName("resetContrasena - error: código incorrecto")
    void resetContrasena_codigoIncorrecto() {
        CodigoRecuperacionEntity codigo = new CodigoRecuperacionEntity();
        codigo.setCodigo("999999");
        codigo.setFechaExpiracion(Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)));
        codigo.setUsado(false);

        when(codigoRepository.findTopByCorreoAndUsadoFalseOrderByFechaExpiracionDesc("juan@correo.com"))
                .thenReturn(Optional.of(codigo));

        assertThatThrownBy(() -> authService.resetContrasena("juan@correo.com", "000000", "NuevaClave1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("incorrecto");
    }

    @Test
    @DisplayName("resetContrasena - error: código expirado")
    void resetContrasena_codigoExpirado() {
        CodigoRecuperacionEntity codigo = new CodigoRecuperacionEntity();
        codigo.setCodigo("123456");
        codigo.setFechaExpiracion(Timestamp.valueOf(LocalDateTime.now().minusMinutes(1)));
        codigo.setUsado(false);

        when(codigoRepository.findTopByCorreoAndUsadoFalseOrderByFechaExpiracionDesc("juan@correo.com"))
                .thenReturn(Optional.of(codigo));

        assertThatThrownBy(() -> authService.resetContrasena("juan@correo.com", "123456", "NuevaClave1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    @DisplayName("resetContrasena - error: no hay código activo")
    void resetContrasena_sinCodigoActivo() {
        when(codigoRepository.findTopByCorreoAndUsadoFalseOrderByFechaExpiracionDesc("juan@correo.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetContrasena("juan@correo.com", "123456", "NuevaClave1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No hay un código");
    }
}