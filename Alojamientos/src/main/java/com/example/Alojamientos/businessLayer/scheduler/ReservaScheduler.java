package com.example.Alojamientos.businessLayer.scheduler;

import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import com.example.Alojamientos.persistenceLayer.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * ReservaScheduler
 *
 * Job automático que cambia a COMPLETADA todas las reservas CONFIRMADAS
 * cuya fecha de checkout (fechaFin) ya pasó.
 *
 * Se ejecuta todos los días a las 00:05 AM.
 * Esto resuelve el problema de reservas que quedan en estado CONFIRMADA
 * después del checkout sin que el sistema las complete automáticamente,
 * lo cual bloqueaba el formulario de comentarios (COMENT-4) y la
 * visualización correcta del historial del huésped.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservaScheduler {

    private final ReservaRepository reservaRepository;

    /**
     * Cron: todos los días a las 00:05 AM
     * "0 5 0 * * *" → segundo=0, minuto=5, hora=0, cualquier día/mes/año
     *
     * Para desarrollo: si quieres que corra cada minuto para probarlo,
     * cambia a: @Scheduled(fixedRate = 60000)
     */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void completarReservasVencidas() {
        LocalDate hoy = LocalDate.now();

        List<ReservaEntity> vencidas = reservaRepository
                .findByEstadoAndFechaFinBefore(
                        ReservaEntity.EstadoReserva.CONFIRMADA,
                        hoy
                );

        if (vencidas.isEmpty()) {
            log.info("[ReservaScheduler] No hay reservas vencidas para completar.");
            return;
        }

        vencidas.forEach(r -> r.setEstado(ReservaEntity.EstadoReserva.COMPLETADA));
        reservaRepository.saveAll(vencidas);

        log.info("[ReservaScheduler] {} reserva(s) marcadas como COMPLETADA.", vencidas.size());
    }
}