package com.example.Alojamientos.config;

import com.example.Alojamientos.securityLayer.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de Spring Security 6.x para la plataforma Hosped.
 *
 * <p>Define:
 * <ul>
 *   <li>Rutas públicas (auth, Swagger/OpenAPI) y rutas protegidas por rol.</li>
 *   <li>Política de sesión stateless (JWT).</li>
 *   <li>Integración del filtro JWT antes del filtro estándar de Spring.</li>C
 *   <li>PasswordEncoder BCrypt y AuthenticationProvider para login.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // Habilita @PreAuthorize / @PostAuthorize en controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    // ─────────────────────────────────────────────────────────────────
    // RUTAS COMPLETAMENTE PÚBLICAS (sin token)
    // ─────────────────────────────────────────────────────────────────

    /** Swagger UI y OpenAPI docs */
    private static final String[] SWAGGER_WHITELIST = {
            "/api-docs/**",
            "/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    /** Endpoints de autenticación y registro */
    private static final String[] AUTH_WHITELIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/recuperar-contrasena",
            "/api/auth/reset-contrasena",
            "/api/usuarios"


    };

    // ─────────────────────────────────────────────────────────────────
    // CADENA DE FILTROS PRINCIPAL
    // ─────────────────────────────────────────────────────────────────

    /**
     * Define la cadena de seguridad HTTP con:
     * <ul>
     *   <li>CSRF deshabilitado (API REST stateless).</li>
     *   <li>CORS delegado a {@link CorsConfig}.</li>
     *   <li>Sesiones stateless (JWT).</li>
     *   <li>Reglas de autorización por rol y método HTTP.</li>
     *   <li>Filtro JWT inyectado antes de {@link UsernamePasswordAuthenticationFilter}.</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // ── CSRF: deshabilitado para APIs REST stateless ──────────────
                .csrf(AbstractHttpConfigurer::disable)

                // ── CORS: usa la configuración de CorsConfig ──────────────────
                .cors(cors -> {})   // delega al bean CorsConfigurationSource / WebMvcConfigurer

                // ── Sesiones: STATELESS, no se crean sesiones HTTP ────────────
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ── Reglas de autorización ─────────────────────────────────────
                .authorizeHttpRequests(auth -> auth

                        // Swagger / OpenAPI — público
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()

                        // Auth — público
                        .requestMatchers(AUTH_WHITELIST).permitAll()

                        // Alojamientos — lectura pública, escritura autenticada
                        .requestMatchers(HttpMethod.GET,  "/api/alojamientos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/alojamientos/**").hasAnyRole("ANFITRION", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,  "/api/alojamientos/**").hasAnyRole("ANFITRION", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/api/alojamientos/**").hasAnyRole("ANFITRION", "ADMIN")

                        // Servicios / imágenes — lectura pública
                        .requestMatchers(HttpMethod.GET, "/api/servicios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/imagenes/**").permitAll()

                        // Comentarios — lectura pública
                        .requestMatchers(HttpMethod.GET, "/api/comentarios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/respuestas-comentarios/**").permitAll()

                        // Promociones — lectura pública
                        .requestMatchers(HttpMethod.GET, "/api/promociones/**").permitAll()

                        // Reservas — solo usuarios autenticados
                        .requestMatchers("/api/reservas/**").authenticated()

                        // Pagos — solo usuarios autenticados
                        .requestMatchers("/api/pagos/**").authenticated()

                        // Favoritos — solo usuarios autenticados
                        .requestMatchers("/api/favoritos/**").authenticated()

                        // Notificaciones — solo usuarios autenticados
                        .requestMatchers("/api/notificaciones/**").authenticated()

                        // Recuperación de contraseña (códigos) — público
                        .requestMatchers("/api/codigos-recuperacion/**").permitAll()

                        // Usuarios — ADMIN para gestión general
                        .requestMatchers(HttpMethod.GET,    "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/usuarios/**").authenticated()

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )

                // ── Proveedor de autenticación personalizado ──────────────────
                .authenticationProvider(authenticationProvider())

                // ── Filtro JWT antes del filtro de usuario/contraseña ─────────
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ─────────────────────────────────────────────────────────────────
    // BEANS DE SOPORTE
    // ─────────────────────────────────────────────────────────────────

    /**
     * Codificador de contraseñas BCrypt (coste por defecto = 10).
     * Usado al registrar usuarios y al validar credenciales en login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Proveedor de autenticación DAO que usa {@link UserDetailsService}
     * y {@link BCryptPasswordEncoder} para validar credenciales.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Expone el {@link AuthenticationManager} como bean para que pueda
     * ser inyectado en el servicio de autenticación (AuthService).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}