package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.CajaMovimientoRequest;
import com.hoteleria.quantum.dto.CajaMovimientoResponse;
import com.hoteleria.quantum.entity.CajaMovimiento;
import com.hoteleria.quantum.entity.Estadia;
import com.hoteleria.quantum.entity.Turno;
import com.hoteleria.quantum.entity.Usuario;
import com.hoteleria.quantum.entity.enums.MetodoPago;
import com.hoteleria.quantum.entity.enums.TipoMovimientoCaja;
import com.hoteleria.quantum.mapper.CajaMovimientoMapper;
import com.hoteleria.quantum.repository.CajaMovimientoRepository;
import com.hoteleria.quantum.repository.TurnoRepository;
import com.hoteleria.quantum.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CajaMovimientoService {

    private final CajaMovimientoRepository cajaMovimientoRepository;
    private final CajaMovimientoMapper cajaMovimientoMapper;
    private final TurnoRepository turnoRepository;
    private final AuditService auditService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public CajaMovimientoResponse registrarMovimiento(CajaMovimientoRequest request, Long usuarioId) {
        // Get active turno for user
        Turno turno = turnoRepository.findActivoByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalStateException(
                        "No hay un turno activo para el usuario. Debe abrir un turno primero."));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + usuarioId));

        CajaMovimiento movimiento = CajaMovimiento.builder()
                .tipo(TipoMovimientoCaja.valueOf(request.getTipo()))
                .metodoPago(MetodoPago.valueOf(request.getMetodoPago()))
                .concepto(request.getConcepto())
                .monto(request.getMonto())
                .usuario(usuario)
                .turno(turno)
                .build();

        if (request.getEstadiaId() != null) {
            Estadia estadia = new Estadia();
            estadia.setId(request.getEstadiaId());
            movimiento.setEstadia(estadia);
        }

        movimiento = cajaMovimientoRepository.save(movimiento);

        auditService.registrarConDetalles(usuarioId, "REGISTRAR_MOVIMIENTO_CAJA", "CajaMovimiento",
                movimiento.getId(),
                Map.of("tipo", request.getTipo(),
                        "monto", request.getMonto().toString(),
                        "concepto", request.getConcepto(),
                        "turnoId", turno.getId()));

        log.info("Movimiento de caja registrado: id={} tipo={} monto={}",
                movimiento.getId(), request.getTipo(), request.getMonto());

        // Force eager load for response
        movimiento.setUsuario(usuario);
        movimiento.setTurno(turno);

        return cajaMovimientoMapper.toResponse(movimiento);
    }

    @Transactional(readOnly = true)
    public List<CajaMovimientoResponse> findByTurnoId(Long turnoId) {
        List<CajaMovimiento> movimientos = cajaMovimientoRepository.findByTurnoId(turnoId);
        movimientos.forEach(this::forceEagerLoad);
        return cajaMovimientoMapper.toResponseList(movimientos);
    }

    @Transactional(readOnly = true)
    public List<CajaMovimientoResponse> findByFecha(LocalDateTime inicio, LocalDateTime fin) {
        List<CajaMovimiento> movimientos = cajaMovimientoRepository.findByFechaMovimientoBetween(inicio, fin);
        movimientos.forEach(this::forceEagerLoad);
        return cajaMovimientoMapper.toResponseList(movimientos);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTotalesPorTurno(Long turnoId) {
        BigDecimal totalIngresos = cajaMovimientoRepository.sumMontoByTipoAndTurnoId(
                TipoMovimientoCaja.INGRESO, turnoId);
        BigDecimal totalEgresos = cajaMovimientoRepository.sumMontoByTipoAndTurnoId(
                TipoMovimientoCaja.EGRESO, turnoId);
        BigDecimal balance = totalIngresos.subtract(totalEgresos);

        Map<String, Object> totales = new HashMap<>();
        totales.put("totalIngresos", totalIngresos);
        totales.put("totalEgresos", totalEgresos);
        totales.put("balance", balance);
        return totales;
    }

    private void forceEagerLoad(CajaMovimiento m) {
        if (m.getUsuario() != null) {
            m.getUsuario().getNombre();
        }
        if (m.getTurno() != null) {
            m.getTurno().getId();
        }
        if (m.getEstadia() != null) {
            m.getEstadia().getCodigo();
        }
        if (m.getPago() != null) {
            m.getPago().getId();
        }
    }
}
