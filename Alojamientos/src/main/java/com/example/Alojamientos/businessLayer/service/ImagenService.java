package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.ImagenDTO;
import com.example.Alojamientos.persistenceLayer.entity.ImagenEntity;
import com.example.Alojamientos.persistenceLayer.mapper.ImagenDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.ImagenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.repository.AlojamientoRepository;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ImagenService {

    private final ImagenRepository imagenRepository;
    private final ImagenDataMapper imagenMapper;
    private final AlojamientoRepository alojamientoRepository; // ← agregado


    /**
     * RF10, RN8: Crear imagen para alojamiento
     * Máximo 10 imágenes por alojamiento
     */
    public ImagenDTO crearImagen(ImagenDTO dto) {
        // Validar que el alojamiento exista
        if (dto.getLodgingId() == null) {
            throw new IllegalArgumentException("El ID del alojamiento es obligatorio");
        }

        // Validar URL no vacía
        if (dto.getUrl() == null || dto.getUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("La URL de la imagen es obligatoria");
        }

        // RN8: Validar límite de 10 imágenes
        long cantidadImagenes = imagenRepository.countByAlojamiento_Id(dto.getLodgingId());
        if (cantidadImagenes >= 10) {
            throw new IllegalArgumentException(
                    "El alojamiento ya tiene el máximo de 10 imágenes permitidas"
            );
        }

// Buscar alojamiento real en la base de datos
        AlojamientoEntity alojamiento = alojamientoRepository.findById(dto.getLodgingId())
                .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado"));

// Mapear DTO a entidad
        ImagenEntity entity = imagenMapper.toEntity(dto);

// Reemplazar el objeto incompleto por el real
        entity.setAlojamiento(alojamiento);

        // Si no se especifica orden, asignar el siguiente disponible
        if (entity.getOrdenVisualizacion() == null || entity.getOrdenVisualizacion() < 0) {
            entity.setOrdenVisualizacion((int) cantidadImagenes);
        }

        ImagenEntity saved = imagenRepository.save(entity);
        return imagenMapper.toDTO(saved);
    }

    /**
     * Listar imágenes de un alojamiento ordenadas
     */
    @Transactional(readOnly = true)
    public List<ImagenDTO> listarPorAlojamiento(Integer alojamientoId) {
        return imagenRepository.findByAlojamiento_IdOrderByOrdenVisualizacionAsc(alojamientoId).stream()
                .map(imagenMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener imagen por ID
     */
    @Transactional(readOnly = true)
    public ImagenDTO obtenerPorId(Integer id) {
        ImagenEntity entity = imagenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada con id: " + id));
        return imagenMapper.toDTO(entity);
    }

    /**
     * Actualizar imagen (descripción u orden)
     */
    public ImagenDTO actualizarImagen(Integer id, ImagenDTO dto) {
        ImagenEntity entity = imagenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada"));

        if (dto.getDescription() != null) {
            entity.setDescripcion(dto.getDescription());
        }
        if (dto.getOrder() != null) {
            entity.setOrdenVisualizacion(dto.getOrder());
        }

        ImagenEntity updated = imagenRepository.save(entity);
        return imagenMapper.toDTO(updated);
    }

    /**
     * Eliminar imagen
     */
    public void eliminarImagen(Integer id) {
        if (!imagenRepository.existsById(id)) {
            throw new IllegalArgumentException("Imagen no encontrada");
        }

        // TODO: Eliminar imagen del servicio externo (Cloudinary, etc.)

        imagenRepository.deleteById(id);
    }
}