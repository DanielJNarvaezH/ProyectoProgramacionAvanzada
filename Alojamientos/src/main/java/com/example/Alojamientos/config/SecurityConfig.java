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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    private static final String[] SWAGGER_WHITELIST = {
            "/api-docs/**",
            "/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private static final String[] AUTH_WHITELIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/recuperar-contrasena",
            "/api/auth/reset-contrasena",
            "/api/usuarios"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // Swagger / OpenAPI — público
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()

                        // Auth — público
                        .requestMatchers(AUTH_WHITELIST).permitAll()

                        // Alojamientos — lectura pública, escritura autenticada
                        .requestMatchers(HttpMethod.GET,    "/api/alojamientos/**").permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/alojamientos/**").hasAnyRole("ANFITRION", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/alojamientos/**").hasAnyRole("ANFITRION", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/alojamientos/**").hasAnyRole("ANFITRION", "ADMIN")

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

                        // Recuperación de contraseña — público
                        .requestMatchers("/api/codigos-recuperacion/**").permitAll()

                        // ── Perfil propio — ANTES de la regla general de usuarios ──
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/me").authenticated()

                        // Usuarios — ADMIN para gestión general
                        .requestMatchers(HttpMethod.GET,    "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/usuarios/**").authenticated()

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}