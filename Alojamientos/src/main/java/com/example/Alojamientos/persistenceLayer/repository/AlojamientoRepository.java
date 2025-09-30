package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.Alojamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlojamientoRepository extends JpaRepository<Alojamiento, Integer> {

    /**
     * Busca alojamientos por ciudad
     * @param ciudad nombre de la ciudad
     * @return lista de alojamientos
     */
    List<Alojamiento> findByCiudadIgnoreCase(String ciudad);

    /**
     * Busca alojamientos por anfitrión
     * @param idAnfitrion id del anfitrión
     * @return lista de alojamientos
     */
    List<Alojamiento> findByIdAnfitrion(Integer idAnfitrion);

    /**
     * Obtiene todos los alojamientos activos
     * @return lista de alojamientos activos
     */
    List<Alojamiento> findByActivoTrue();

    /**
     * Busca alojamientos con capacidad mayor o igual al número de huéspedes
     * @param capacidad número mínimo de huéspedes
     * @return lista de alojamientos que cumplen
     */
    List<Alojamiento> findByCapacidadMaximaGreaterThanEqual(Integer capacidad);

    /**
     * Busca alojamientos por rango de precio
     * @param precioMin precio mínimo
     * @param precioMax precio máximo
     * @return lista de alojamientos en ese rango
     */
    List<Alojamiento> findByPrecioPorNocheBetween(Double precioMin, Double precioMax);
}
