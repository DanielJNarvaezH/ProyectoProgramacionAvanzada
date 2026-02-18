package com.example.Alojamientos.securityLayer;

import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import com.example.Alojamientos.persistenceLayer.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de {@link UserDetailsService} para Spring Security.
 *
 * <p>Carga el usuario desde la base de datos usando el correo electrónico
 * (que actúa como "username" en toda la plataforma Hosped).
 *
 * <p>Spring Security llama a este servicio automáticamente durante el proceso
 * de autenticación a través del {@code DaoAuthenticationProvider} configurado
 * en {@code SecurityConfig}.
 *
 * <p>También es usado por {@link JwtAuthenticationFilter} para reconstruir
 * el {@link UserDetails} a partir del email extraído del token JWT.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga el usuario por correo electrónico.
     *
     * <p>Convierte el {@link UsuarioEntity} en un {@link UserDetails} de Spring Security,
     * mapeando el rol del enum {@code UsuarioEntity.Rol} al formato esperado por Spring
     * ({@code ROLE_USUARIO}, {@code ROLE_ANFITRION}, {@code ROLE_ADMIN}).
     *
     * @param correo correo electrónico del usuario (username en la plataforma)
     * @return {@link UserDetails} con correo, contraseña hasheada y autoridad de rol
     * @throws UsernameNotFoundException si no existe un usuario con ese correo
     *                                   o si el usuario está inactivo (soft-delete)
     */
    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No se encontró un usuario con el correo: " + correo
                ));

        // Validación de cuenta activa (soft-delete)
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new UsernameNotFoundException(
                    "La cuenta del usuario '" + correo + "' está desactivada"
            );
        }

        // Convierte el Rol del enum → "ROLE_USUARIO" / "ROLE_ANFITRION" / "ROLE_ADMIN"
        String authority = "ROLE_" + usuario.getRol().name();

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getContrasena())          // ya es BCrypt desde el registro
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}