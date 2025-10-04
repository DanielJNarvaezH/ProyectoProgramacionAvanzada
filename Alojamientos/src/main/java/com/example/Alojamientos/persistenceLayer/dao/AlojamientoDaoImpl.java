package com.example.Alojamientos.persistenceLayer.dao.impl;

import com.example.Alojamientos.persistenceLayer.dao.AlojamientoDao;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.repository.AlojamientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AlojamientoDaoImpl implements AlojamientoDao {

    private final AlojamientoRepository alojamientoRepository;

    @Override
    public Optional<AlojamientoEntity> findById(Integer id) {
        return alojamientoRepository.findById(id);
    }

    @Override
    public List<AlojamientoEntity> findByCiudad(String ciudad) {
        return alojamientoRepository.findByCiudadIgnoreCase(ciudad);
    }

    @Override
    public List<AlojamientoEntity> findByAnfitrionId(Integer idAnfitrion) {
        return alojamientoRepository.findByIdAnfitrion(idAnfitrion);
    }

    @Override
    public List<AlojamientoEntity> findActivos() {
        return alojamientoRepository.findByActivoTrue();
    }

    @Override
    public List<AlojamientoEntity> findByCapacidadMinima(Integer capacidad) {
        return alojamientoRepository.findByCapacidadMaximaGreaterThanEqual(capacidad);
    }

    @Override
    public List<AlojamientoEntity> findByRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        // Convertir BigDecimal a Double para el repositorio
        return alojamientoRepository.findByPrecioPorNocheBetween(
                precioMin.doubleValue(),
                precioMax.doubleValue()
        );
    }

    @Override
    public AlojamientoEntity save(AlojamientoEntity alojamiento) {
        return alojamientoRepository.save(alojamiento);
    }

    @Override
    public void deleteById(Integer id) {
        alojamientoRepository.deleteById(id);
    }
}
