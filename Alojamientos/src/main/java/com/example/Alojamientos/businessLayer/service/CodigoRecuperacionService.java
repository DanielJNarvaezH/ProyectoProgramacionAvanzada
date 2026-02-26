package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.CodigoRecuperacionDTO;
import com.example.Alojamientos.persistenceLayer.entity.CodigoRecuperacionEntity;
import com.example.Alojamientos.persistenceLayer.mapper.CodigoRecuperacionDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.CodigoRecuperacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class CodigoRecuperacionService {

    private final CodigoRecuperacionRepository codigoRepository;
    private final CodigoRecuperacionDataMapper codigoMapper;

    /**
     * RF5, HU-005: Generar código de recuperación de contraseña
     * RN7: Código válido por 15 minutos
     */
    public CodigoRecuperacionDTO generarCodigo(Integer usuarioId) {
        // Invalidar códigos anteriores no usados del usuario
        List<CodigoRecuperacionEntity> codigosAnteriores =
                codigoRepository.findByUsuario_IdAndUsadoFalse(usuarioId);

        codigosAnteriores.forEach(c -> c.setUsado(true));
        codigoRepository.saveAll(codigosAnteriores);

        // Generar nuevo código de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(999999));

        // Crear entidad
        CodigoRecuperacionEntity entity = new CodigoRecuperacionEntity();
        entity.setUsuario(com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity.builder()
                .id(usuarioId).build());
        entity.setCodigo(codigo);
        entity.setFechaCreacion(Timestamp.valueOf(LocalDateTime.now()));
        // RN7: Expira en 15 minutos
        entity.setFechaExpiracion(Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)));
        entity.setUsado(false);

        CodigoRecuperacionEntity saved = codigoRepository.save(entity);

        // TODO: Enviar código por email usando JavaMailSender

        return codigoMapper.toDTO(saved);
    }

    /**
     * RF5: Validar código de recuperación
     * RN7: Debe estar vigente y no usado
     */
    @Transactional(readOnly = true)
    public boolean validarCodigo(String codigo, Integer usuarioId) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }

        if (usuarioId == null) {
            return false;
        }

        CodigoRecuperacionEntity entity = codigoRepository
                .findByCodigoAndUsuario_IdAndUsadoFalse(codigo, usuarioId)
                .orElse(null);

        if (entity == null) {
            return false;
        }

        // RN7: Verificar que no esté expirado (15 minutos)
        Timestamp ahora = Timestamp.valueOf(LocalDateTime.now());
        boolean noExpirado = entity.getFechaExpiracion().after(ahora);

        if (!noExpirado) {
            // El código expiró, marcarlo como usado para limpieza
            entity.setUsado(true);
            codigoRepository.save(entity);
        }

        return noExpirado;
    }

    /**
     * Marcar código como usado
     */
    public void marcarComoUsado(String codigo, Integer usuarioId) {
        CodigoRecuperacionEntity entity = codigoRepository
                .findByCodigoAndUsuario_IdAndUsadoFalse(codigo, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Código no válido"));

        // Verificar que no esté expirado
        if (entity.getFechaExpiracion().before(Timestamp.valueOf(LocalDateTime.now()))) {
            throw new IllegalArgumentException("El código ha expirado");
        }

        entity.setUsado(true);
        codigoRepository.save(entity);
    }

    /**
     * Obtener código por ID
     */
    @Transactional(readOnly = true)
    public CodigoRecuperacionDTO obtenerPorId(Long id) {
        CodigoRecuperacionEntity entity = codigoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Código no encontrado con id: " + id));
        return codigoMapper.toDTO(entity);
    }

    /**
     * Limpiar códigos expirados (tarea programada)
     */
    public void limpiarCodigosExpirados() {
        Timestamp ahora = Timestamp.valueOf(LocalDateTime.now());
        List<CodigoRecuperacionEntity> expirados =
                codigoRepository.findByFechaExpiracionBeforeAndUsadoFalse(ahora);

        codigoRepository.deleteAll(expirados);
    }
}