package com.example.Alojamientos.securityLayer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT que se ejecuta una sola vez por request (extiende {@link OncePerRequestFilter}).
 *
 * <p>Flujo de validación:
 * <ol>
 *   <li>Extrae el header {@code Authorization: Bearer <token>}.</li>
 *   <li>Saca el email del token usando {@link JwtService#extraerEmail(String)}.</li>
 *   <li>Valida que el token sea correcto y no esté expirado.</li>
 *   <li>Si todo es válido, registra la autenticación en el {@link SecurityContextHolder}
 *       para que Spring Security permita el acceso al endpoint protegido.</li>
 * </ol>
 *
 * <p>Este filtro es registrado en {@code SecurityConfig} justo antes de
 * {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER      = "Authorization";
    private static final String BEARER_PREFIX     = "Bearer ";

    private final JwtService         jwtService;
    private final UserDetailsService userDetailsService;  // → UserDetailsServiceImpl

    /**
     * Lógica principal del filtro.
     *
     * <p>Si no hay header {@code Authorization} o no empieza con {@code Bearer },
     * la request continúa sin autenticación (las rutas públicas pasarán igual).
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         filterChain
    ) throws ServletException, IOException {

        // ── 1. Leer el header Authorization ──────────────────────────────
        final String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            // Sin token → continúa sin autenticar (rutas públicas funcionan igual)
            filterChain.doFilter(request, response);
            return;
        }

        // ── 2. Extraer el token (quitar "Bearer ") ────────────────────────
        final String token = authHeader.substring(BEARER_PREFIX.length());

        // ── 3. Validar estructura básica antes de procesar ────────────────
        if (!jwtService.esTokenEstructuralmenteValido(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── 4. Extraer el email (subject) del token ───────────────────────
        final String correo = jwtService.extraerEmail(token);

        // ── 5. Solo proceder si hay email Y el contexto aún no tiene auth ─
        if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Cargar el usuario desde la base de datos
            UserDetails userDetails = userDetailsService.loadUserByUsername(correo);

            // Validar token contra el usuario cargado (firma + expiración + email)
            if (jwtService.esTokenValido(token, userDetails.getUsername())) {

                // Construir el objeto de autenticación de Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // sin credenciales (ya autenticado)
                                userDetails.getAuthorities()   // roles: ROLE_USUARIO, ROLE_ANFITRION, ROLE_ADMIN
                        );

                // Agregar detalles de la request (IP, session, etc.)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Registrar la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // ── 6. Continuar con el siguiente filtro de la cadena ─────────────
        filterChain.doFilter(request, response);
    }
}