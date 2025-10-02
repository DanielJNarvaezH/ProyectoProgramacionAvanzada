package com.example.Alojamientos.persistenceLayer.dao.impl;

import com.example.Alojamientos.persistenceLayer.dao.ImagenDAO;
import com.example.Alojamientos.persistenceLayer.entity.ImagenEntity;
import com.example.Alojamientos.persistenceLayer.repository.ImagenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ImagenDaoImpl implements ImagenDao {

    private final ImagenRepository imagenRepository;

    @Override
    public Optional<ImagenEntity> findById(Integer id) {
        return imagenRepository.findById(id);
    }

    @Override
    public List<ImagenEntity> findByAlojamiento(Integer idAlojamiento) {
        return imagenRepository.findByIdAlojamiento(idAlojamiento);
    }

    @Override
    public ImagenEntity save(ImagenEntity imagen) {
        return imagenRepository.save(imagen);
    }

    @Override
    public void deleteById(Integer id) {
        imagenRepository.deleteById(id);
    }

    @Override
    public void deleteByAlojamiento(Integer idAlojamiento) {
        imagenRepository.deleteByIdAlojamiento(idAlojamiento);
    }

    @Override
    public boolean existsByAlojamiento(Integer idAlojamiento) {
        return imagenRepository.existsByIdAlojamiento(idAlojamiento);
    }
}
