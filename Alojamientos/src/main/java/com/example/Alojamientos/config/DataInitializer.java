package com.example.Alojamientos.config;

import com.example.Alojamientos.persistenceLayer.entity.ServicioEntity;
import com.example.Alojamientos.persistenceLayer.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DataInitializer — Seed automático de datos base al arrancar la aplicación.
 *
 * Inserta los servicios oficiales de Hosped si no existen en la BD.
 * Es idempotente: si el servicio ya existe (por nombre) no lo duplica.
 * Se ejecuta después de que Hibernate crea/actualiza las tablas (ddl-auto=update).
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ServicioRepository servicioRepository;

    @Override
    public void run(String... args) {
        seedServicios();
    }

    private void seedServicios() {
        List<ServicioEntity> serviciosOficiales = List.of(
                buildServicio("WiFi",               "Internet de alta velocidad",           "fa-solid fa-wifi"),
                buildServicio("Piscina",            "Piscina privada o compartida",         "fa-solid fa-water-ladder"),
                buildServicio("Parqueadero",        "Estacionamiento gratuito",             "fa-solid fa-square-parking"),
                buildServicio("Aire acondicionado", "Sistema de climatización",             "fa-solid fa-wind"),
                buildServicio("Cocina equipada",    "Cocina equipada para uso del huésped", "fa-solid fa-utensils"),
                buildServicio("TV",                 "Televisión con cable o streaming",     "fa-solid fa-tv"),
                buildServicio("Lavadora",           "Lavadora disponible",                  "fa-solid fa-jug-detergent"),
                buildServicio("Mascotas",           "Se permiten mascotas",                 "fa-solid fa-paw"),
                buildServicio("Terraza",            "Terraza privada o compartida",         "fa-solid fa-sun")
        );

        int insertados = 0;
        for (ServicioEntity servicio : serviciosOficiales) {
            if (!servicioRepository.existsByNombre(servicio.getNombre())) {
                servicioRepository.save(servicio);
                insertados++;
            }
        }

        if (insertados > 0) {
            log.info("DataInitializer: {} servicio(s) insertado(s) correctamente.", insertados);
        } else {
            log.info("DataInitializer: Todos los servicios ya existen en la BD. Nada que insertar.");
        }
    }

    private ServicioEntity buildServicio(String nombre, String descripcion, String icono) {
        return ServicioEntity.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .icono(icono)
                .activo(true)
                .build();
    }
}