package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.CardexCierreResponse;
import com.hoteleria.quantum.entity.CardexCierre;
import com.hoteleria.quantum.entity.CajaMovimiento;
import com.hoteleria.quantum.entity.Turno;
import com.hoteleria.quantum.entity.Usuario;
import com.hoteleria.quantum.entity.enums.EstadoEstadia;
import com.hoteleria.quantum.entity.enums.EstadoTurno;
import com.hoteleria.quantum.entity.enums.TipoMovimientoCaja;
import com.hoteleria.quantum.mapper.CardexCierreMapper;
import com.hoteleria.quantum.repository.CajaMovimientoRepository;
import com.hoteleria.quantum.repository.CardexCierreRepository;
import com.hoteleria.quantum.repository.EstadiaRepository;
import com.hoteleria.quantum.repository.TurnoRepository;
import com.hoteleria.quantum.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardexService {

    private final CardexCierreRepository cardexCierreRepository;
    private final CardexCierreMapper cardexCierreMapper;
    private final CajaMovimientoRepository cajaMovimientoRepository;
    private final TurnoRepository turnoRepository;
    private final EstadiaRepository estadiaRepository;
    private final AuditService auditService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public CardexCierreResponse generarCierre(Long turnoId, String observaciones, Long usuarioId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Turno no encontrado con id: " + turnoId));

        if (turno.getEstado() != EstadoTurno.CERRADO) {
            throw new IllegalStateException(
                    "El turno debe estar CERRADO para generar el cierre. Estado actual: " + turno.getEstado());
        }

        // Check if cierre already exists
        cardexCierreRepository.findByTurnoId(turnoId).ifPresent(c -> {
            throw new IllegalStateException(
                    "Ya existe un cierre de cárdex para el turno id=" + turnoId);
        });

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + usuarioId));

        // Calculate totals from CajaMovimiento
        BigDecimal totalIngresos = cajaMovimientoRepository.sumMontoByTipoAndTurnoId(
                TipoMovimientoCaja.INGRESO, turnoId);
        BigDecimal totalEgresos = cajaMovimientoRepository.sumMontoByTipoAndTurnoId(
                TipoMovimientoCaja.EGRESO, turnoId);
        BigDecimal balance = totalIngresos.subtract(totalEgresos);

        // Calculate desglose by metodo_pago
        List<CajaMovimiento> movimientos = cajaMovimientoRepository.findByTurnoId(turnoId);
        Map<String, Object> desgloseMetodos = new HashMap<>();
        movimientos.forEach(m -> {
            String metodo = m.getMetodoPago() != null ? m.getMetodoPago().name() : "SIN_METODO";
            BigDecimal current = desgloseMetodos.containsKey(metodo)
                    ? new BigDecimal(desgloseMetodos.get(metodo).toString())
                    : BigDecimal.ZERO;
            if (m.getTipo() == TipoMovimientoCaja.INGRESO) {
                desgloseMetodos.put(metodo, current.add(m.getMonto()));
            } else {
                desgloseMetodos.put(metodo, current.subtract(m.getMonto()));
            }
        });

        // Count estadias registradas and checkouts during the turno
        LocalDateTime turnoInicio = turno.getFecha().atTime(turno.getHoraInicio());
        LocalDateTime turnoFin = turno.getHoraFin() != null
                ? turno.getFecha().atTime(turno.getHoraFin())
                : turno.getFecha().atTime(LocalTime.MAX);

        Long estadiasRegistradas = estadiaRepository.countByEstadoAndFechaRegistroBetween(
                EstadoEstadia.REGISTRADA, turnoInicio, turnoFin)
                + estadiaRepository.countByEstadoAndFechaRegistroBetween(
                EstadoEstadia.ACTIVA, turnoInicio, turnoFin);

        Long checkoutsRealizados = estadiaRepository.countByEstadoAndFechaRegistroBetween(
                EstadoEstadia.COMPLETADA, turnoInicio, turnoFin);

        CardexCierre cierre = CardexCierre.builder()
                .fecha(turno.getFecha())
                .turno(turno)
                .usuarioCierre(usuario)
                .totalIngresos(totalIngresos)
                .totalEgresos(totalEgresos)
                .balance(balance)
                .desgloseMetodos(desgloseMetodos)
                .estadiasRegistradas(estadiasRegistradas.intValue())
                .checkoutsRealizados(checkoutsRealizados.intValue())
                .observaciones(observaciones)
                .build();

        cierre = cardexCierreRepository.save(cierre);

        auditService.registrarConDetalles(usuarioId, "GENERAR_CIERRE_CARDEX", "CardexCierre",
                cierre.getId(),
                Map.of("turnoId", turnoId,
                        "totalIngresos", totalIngresos.toString(),
                        "totalEgresos", totalEgresos.toString(),
                        "balance", balance.toString()));

        log.info("Cierre de cárdex generado: id={} turnoId={} balance={}",
                cierre.getId(), turnoId, balance);
        return cardexCierreMapper.toResponse(cierre);
    }

    @Transactional(readOnly = true)
    public CardexCierreResponse findByTurnoId(Long turnoId) {
        CardexCierre cierre = cardexCierreRepository.findByTurnoId(turnoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cierre de cárdex no encontrado para turno id: " + turnoId));
        forceEagerLoad(cierre);
        return cardexCierreMapper.toResponse(cierre);
    }

    @Transactional(readOnly = true)
    public List<CardexCierreResponse> findByFecha(LocalDate fecha) {
        List<CardexCierre> cierres = cardexCierreRepository.findByFecha(fecha);
        cierres.forEach(this::forceEagerLoad);
        return cardexCierreMapper.toResponseList(cierres);
    }

    @Transactional(readOnly = true)
    public List<CardexCierreResponse> findByRango(LocalDate inicio, LocalDate fin) {
        List<CardexCierre> cierres = cardexCierreRepository.findByFechaBetween(inicio, fin);
        cierres.forEach(this::forceEagerLoad);
        return cardexCierreMapper.toResponseList(cierres);
    }

    private void forceEagerLoad(CardexCierre cierre) {
        if (cierre.getTurno() != null) {
            cierre.getTurno().getId();
        }
        if (cierre.getUsuarioCierre() != null) {
            cierre.getUsuarioCierre().getNombre();
        }
    }
}
