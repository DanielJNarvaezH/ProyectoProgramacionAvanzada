package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.FavoritoDTO;
import com.example.Alojamientos.persistenceLayer.entity.FavoritoEntity;
import com.example.Alojamientos.persistenceLayer.mapper.FavoritoDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.FavoritoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final FavoritoDataMapper favoritoMapper;

    /**
     * RF36, HU-031: Marcar alojamiento como favorito
     * RN29: Solo usuarios huésped
     */
    public FavoritoDTO agregarFavorito(FavoritoDTO dto) {
        // Validar que no exista ya como favorito
        if (favoritoRepository.existsByUsuario_IdAndAlojamiento_Id(dto.getUserId(), dto.getLodgingId())) {
            throw new IllegalArgumentException("Este alojamiento ya está en tus favoritos");
        }

        FavoritoEntity entity = favoritoMapper.toEntity(dto);
        FavoritoEntity saved = favoritoRepository.save(entity);
        return favoritoMapper.toDTO(saved);
    }

    /**
     * RF37, HU-032: Listar favoritos de un usuario
     */
    @Transactional(readOnly = true)
    public List<FavoritoDTO> listarPorUsuario(Integer usuarioId) {
        return favoritoRepository.findByUsuario_IdOrderByFechaAgregadoDesc(usuarioId).stream()
                .map(favoritoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * RF38, HU-033: Contar favoritos de un alojamiento
     * RN30: Anfitrión puede ver cantidad pero no identidades
     */
    @Transactional(readOnly = true)
    public Long contarFavoritosPorAlojamiento(Integer alojamientoId) {
        return favoritoRepository.countByAlojamiento_Id(alojamientoId);
    }

    /**
     * Eliminar favorito
     */
    public void eliminarFavorito(Integer id) {
        if (!favoritoRepository.existsById(id)) {
            throw new IllegalArgumentException("Favorito no encontrado");
        }
        favoritoRepository.deleteById(id);
    }

    /**
     * Eliminar favorito por usuario y alojamiento
     */
    public void eliminarFavoritoPorUsuarioYAlojamiento(Integer usuarioId, Integer alojamientoId) {
        FavoritoEntity favorito = favoritoRepository.findByUsuario_IdAndAlojamiento_Id(usuarioId, alojamientoId)
                .orElseThrow(() -> new IllegalArgumentException("Favorito no encontrado"));

        favoritoRepository.deleteById(favorito.getId());
    }

    /**
     * Verificar si un alojamiento es favorito de un usuario
     */
    @Transactional(readOnly = true)
    public boolean esFavorito(Integer usuarioId, Integer alojamientoId) {
        return favoritoRepository.existsByUsuario_IdAndAlojamiento_Id(usuarioId, alojamientoId);
    }
}