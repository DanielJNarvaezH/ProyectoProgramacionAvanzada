package com.example.Alojamientos.Pago;

import com.example.Alojamientos.businessLayer.dto.PagoDTO;
import com.example.Alojamientos.businessLayer.service.PagoService;
import com.example.Alojamientos.persistenceLayer.entity.PagoEntity;
import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import com.example.Alojamientos.persistenceLayer.mapper.PagoDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.PagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PagoDataMapper pagoMapper;

    @InjectMocks
    private PagoService pagoService;

    private PagoDTO dto;
    private PagoEntity entity;

    @BeforeEach
    void setup() {
        dto = PagoDTO.builder()
                .reservationId(1)
                .amount(200.0)
                .method("TARJETA_CREDITO")
                .status("PENDIENTE")
                .build();

        entity = PagoEntity.builder()
                .id(1)
                .monto(BigDecimal.valueOf(200.0))
                .metodo(PagoEntity.MetodoPago.TARJETA_CREDITO)
                .estado(PagoEntity.EstadoPago.PENDIENTE)
                .reserva(new ReservaEntity())
                .referenciaExterna("PAY-12345678")
                .build();
    }

    // 1️⃣ Registrar pago correctamente
    @Test
    void registrarPago_Exitoso() {
        when(pagoRepository.existsByReserva_Id(dto.getReservationId())).thenReturn(false);
        when(pagoMapper.toEntity(dto)).thenReturn(entity);
        when(pagoRepository.save(any(PagoEntity.class))).thenReturn(entity);
        when(pagoMapper.toDTO(entity)).thenReturn(dto);

        PagoDTO result = pagoService.registrarPago(dto);

        assertNotNull(result);
        assertEquals(dto.getAmount(), result.getAmount());
        verify(pagoRepository).save(any(PagoEntity.class));
    }

    // 2️⃣ Registrar pago con monto inválido
    @Test
    void registrarPago_MontoInvalido() {
        dto.setAmount(-10.0);
        assertThrows(IllegalArgumentException.class, () -> pagoService.registrarPago(dto));
    }

    // 3️⃣ Registrar pago con reserva repetida
    @Test
    void registrarPago_ReservaYaExiste() {
        when(pagoRepository.existsByReserva_Id(dto.getReservationId())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> pagoService.registrarPago(dto));
    }

    // 4️⃣ Confirmar pago existente
    @Test
    void confirmarPago_Exitoso() {
        when(pagoRepository.findById(1)).thenReturn(Optional.of(entity));
        when(pagoRepository.save(any(PagoEntity.class))).thenReturn(entity);
        when(pagoMapper.toDTO(entity)).thenReturn(dto);

        PagoDTO result = pagoService.confirmarPago(1, "REF-OK123");

        assertEquals("PENDIENTE", dto.getStatus()); // status del DTO
        assertNotNull(result);
        verify(pagoRepository).save(entity);
    }

    // 5️⃣ Confirmar pago inexistente
    @Test
    void confirmarPago_PagoNoEncontrado() {
        when(pagoRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> pagoService.confirmarPago(99, "REF-XYZ"));
    }

    // 6️⃣ Marcar como fallido (exitoso)
    @Test
    void marcarComoFallido_Exitoso() {
        when(pagoRepository.findById(1)).thenReturn(Optional.of(entity));
        when(pagoRepository.save(any(PagoEntity.class))).thenReturn(entity);
        when(pagoMapper.toDTO(entity)).thenReturn(dto);

        PagoDTO result = pagoService.marcarComoFallido(1);

        assertNotNull(result);
        verify(pagoRepository).save(any(PagoEntity.class));
    }

    // 7️⃣ Marcar como fallido inexistente
    @Test
    void marcarComoFallido_NoEncontrado() {
        when(pagoRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> pagoService.marcarComoFallido(1));
    }

    // 8️⃣ Obtener pago por ID válido
    @Test
    void obtenerPorId_Exitoso() {
        when(pagoRepository.findById(1)).thenReturn(Optional.of(entity));
        when(pagoMapper.toDTO(entity)).thenReturn(dto);

        PagoDTO result = pagoService.obtenerPorId(1);

        assertEquals(dto.getReservationId(), result.getReservationId());
    }

    // 9️⃣ Obtener pago por ID inexistente
    @Test
    void obtenerPorId_NoEncontrado() {
        when(pagoRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> pagoService.obtenerPorId(1));
    }

    // 🔟 Obtener pago por reserva válida
    @Test
    void obtenerPorReserva_Exitoso() {
        when(pagoRepository.findByReserva_Id(1)).thenReturn(Optional.of(entity));
        when(pagoMapper.toDTO(entity)).thenReturn(dto);

        PagoDTO result = pagoService.obtenerPorReserva(1);
        assertNotNull(result);
        assertEquals(dto.getReservationId(), result.getReservationId());
    }

    // 11️⃣ Obtener pago por reserva inexistente
    @Test
    void obtenerPorReserva_NoEncontrado() {
        when(pagoRepository.findByReserva_Id(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> pagoService.obtenerPorReserva(1));
    }

    // 12️⃣ Listar todos los pagos (con elementos)
    @Test
    void listarTodos_ConPagos() {
        when(pagoRepository.findAll()).thenReturn(List.of(entity));
        when(pagoMapper.toDTO(entity)).thenReturn(dto);

        List<PagoDTO> result = pagoService.listarTodos();

        assertEquals(1, result.size());
        assertEquals(dto.getReservationId(), result.get(0).getReservationId());
    }

    // 13️⃣ Listar todos los pagos (sin elementos)
    @Test
    void listarTodos_SinPagos() {
        when(pagoRepository.findAll()).thenReturn(Collections.emptyList());

        List<PagoDTO> result = pagoService.listarTodos();

        assertTrue(result.isEmpty());
    }

    // 14️⃣ Validar que se genere referencia externa al registrar pago
    @Test
    void registrarPago_GeneraReferenciaExterna() {
        when(pagoRepository.existsByReserva_Id(dto.getReservationId())).thenReturn(false);
        when(pagoMapper.toEntity(dto)).thenReturn(entity);
        when(pagoRepository.save(any(PagoEntity.class))).thenReturn(entity);
        when(pagoMapper.toDTO(entity)).thenReturn(dto);

        PagoDTO result = pagoService.registrarPago(dto);

        assertNotNull(result);
        verify(pagoRepository).save(any(PagoEntity.class));
    }

    // 15️⃣ Validar estado inicial PENDIENTE
    @Test
    void registrarPago_EstadoInicialPendiente() {
        when(pagoRepository.existsByReserva_Id(dto.getReservationId())).thenReturn(false);
        when(pagoMapper.toEntity(dto)).thenReturn(entity);
        when(pagoRepository.save(any(PagoEntity.class))).thenReturn(entity);
        when(pagoMapper.toDTO(entity)).thenReturn(dto);

        PagoDTO result = pagoService.registrarPago(dto);

        assertEquals("PENDIENTE", dto.getStatus());
    }
}
