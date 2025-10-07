package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.UsuarioDTO;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import com.example.Alojamientos.persistenceLayer.mapper.UsuarioDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioDataMapper usuarioMapper;
    // Descomentar cuando agregues Spring Security
    // private final PasswordEncoder passwordEncoder;

    /**
     * RF1: Registrar un nuevo usuario
     * RN1: Email debe ser único
     * RN2: Validar política de contraseñas
     * RN3: Usuario debe ser mayor de 18 años para ser anfitrión
     */
    public UsuarioDTO crearUsuario(UsuarioDTO dto) {
        // RN1: Validar email único
        if (usuarioRepository.existsByCorreo(dto.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // RN2: Validar contraseña (ya validada por @Size en DTO, pero reforzamos)
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        // RN3: Validar edad si es anfitrión
        if ("ANFITRION".equalsIgnoreCase(dto.getRole()) || "HOST".equalsIgnoreCase(dto.getRole())) {
            LocalDate birthDate = LocalDate.parse(dto.getBirthDate());
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age < 18) {
                throw new IllegalArgumentException("Debes ser mayor de 18 años para registrarte como anfitrión");
            }
        }

        // Convertir DTO a Entity
        UsuarioEntity entity = usuarioMapper.toEntity(dto);

        // RN2: Encriptar contraseña (por ahora comentado, activar con Spring Security)
        // entity.setContrasena(passwordEncoder.encode(dto.getPassword()));

        // Guardar
        UsuarioEntity saved = usuarioRepository.save(entity);

        // TODO: RF3 - Enviar email de verificación (implementar con JavaMailSender)

        return usuarioMapper.toDTO(saved);
    }

    /**
     * RF3, RF7: Obtener usuario por ID
     */
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Integer id) {
        UsuarioEntity entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));
        return usuarioMapper.toDTO(entity);
    }

    /**
     * RF7: Editar perfil de usuario
     * RN6: Solo el propio usuario puede modificar sus datos
     */
    public UsuarioDTO actualizarUsuario(Integer id, UsuarioDTO dto) {
        UsuarioEntity entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));

        // Actualizar solo campos permitidos (no contraseña ni email)
        if (dto.getName() != null) {
            entity.setNombre(dto.getName());
        }
        if (dto.getPhone() != null) {
            entity.setTelefono(dto.getPhone());
        }
        // Foto y descripción se actualizan si vienen
        // entity.setFoto(dto.getFoto());
        // entity.setDescripcion(dto.getDescripcion());

        UsuarioEntity updated = usuarioRepository.save(entity);
        return usuarioMapper.toDTO(updated);
    }

    /**
     * RF6: Cambiar contraseña (usuario autenticado)
     */
    public void cambiarContrasena(Integer userId, String oldPassword, String newPassword) {
        UsuarioEntity entity = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validar contraseña actual
        // if (!passwordEncoder.matches(oldPassword, entity.getContrasena())) {
        //     throw new IllegalArgumentException("Contraseña actual incorrecta");
        // }

        // RN2: Validar nueva contraseña
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        // Encriptar y guardar
        // entity.setContrasena(passwordEncoder.encode(newPassword));
        usuarioRepository.save(entity);
    }

    /**
     * RN4: Eliminar usuario (validar que no tenga alojamientos activos o reservas futuras)
     */
    public void eliminarUsuario(Integer id) {
        UsuarioEntity entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));

        // RN4: Validar que no tenga alojamientos activos o reservas futuras
        // TODO: Implementar validación con AlojamientoService y ReservaService

        entity.setActivo(false);
        usuarioRepository.save(entity);
    }

    /**
     * RF3: Buscar usuario por email (para login)
     */
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorEmail(String email) {
        UsuarioEntity entity = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));
        return usuarioMapper.toDTO(entity);
    }

    /**
     * Listar todos los usuarios activos
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .filter(UsuarioEntity::getActivo)
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Validar si un email ya existe
     */
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByCorreo(email);
    }
}