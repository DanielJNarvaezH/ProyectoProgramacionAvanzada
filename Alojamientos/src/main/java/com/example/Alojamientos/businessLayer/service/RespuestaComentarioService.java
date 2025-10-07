package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.RespuestaComentarioDTO;
import com.example.Alojamientos.persistenceLayer.entity.ComentarioEntity;
import com.example.Alojamientos.persistenceLayer.entity.RespuestaComentarioEntity;
import com.example.Alojamientos.persistenceLayer.mapper.RespuestaComentarioDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.ComentarioRepository;
import com.example.Alojamientos.persistenceLayer.repository.RespuestaComentarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RespuestaComentarioService {

    private final RespuestaComentarioRepository respuestaRepository;
    private final ComentarioRepository comentarioRepository;
    private final RespuestaComentarioDataMapper respuestaMapper;

    /**
     * RF26, HU-026: Crear respuesta a comentario
     * Máximo 1 respuesta por comentario (según doc HU-026)
     */
    public RespuestaComentarioDTO crearRespuesta(RespuestaComentarioDTO dto) {
        // Validar que el comentario existe
        ComentarioEntity comentario = comentarioRepository.findById(dto.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("Comentario no encontrado"));

        // Validar que no exista respuesta previa
        if (respuestaRepository.existsByComentario_Id(dto.getCommentId())) {
            throw new IllegalArgumentException("Ya existe una respuesta para este comentario");
        }

        // Validar longitud texto
        if (dto.getText().length() > 500) {
            throw new IllegalArgumentException("La respuesta no puede exceder 500 caracteres");
        }

        // Crear entidad
        RespuestaComentarioEntity entity = respuestaMapper.toEntity(dto);
        RespuestaComentarioEntity saved = respuestaRepository.save(entity);

        return respuestaMapper.toDTO(saved);
    }

    /**
     * Listar respuestas de un comentario específico
     */
    @Transactional(readOnly = true)
    public List<RespuestaComentarioDTO> listarPorComentario(Integer comentarioId) {
        return respuestaRepository.findByComentario_Id(comentarioId).stream()
                .map(respuestaMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener respuesta por ID
     */
    @Transactional(readOnly = true)
    public RespuestaComentarioDTO obtenerPorId(Integer id) {
        RespuestaComentarioEntity entity = respuestaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Respuesta no encontrada con id: " + id));
        return respuestaMapper.toDTO(entity);
    }

    /**
     * Actualizar respuesta
     */
    public RespuestaComentarioDTO actualizarRespuesta(Integer id, String nuevoTexto) {
        RespuestaComentarioEntity entity = respuestaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Respuesta no encontrada"));

        if (nuevoTexto.length() > 500) {
            throw new IllegalArgumentException("La respuesta no puede exceder 500 caracteres");
        }

        entity.setTexto(nuevoTexto);
        RespuestaComentarioEntity updated = respuestaRepository.save(entity);
        return respuestaMapper.toDTO(updated);
    }

    /**
     * Eliminar respuesta
     */
    public void eliminarRespuesta(Integer id) {
        if (!respuestaRepository.existsById(id)) {
            throw new IllegalArgumentException("Respuesta no encontrada");
        }
        respuestaRepository.deleteById(id);
    }
}