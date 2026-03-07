package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.PagoRequest;
import com.hoteleria.quantum.dto.PagoResponse;
import com.hoteleria.quantum.entity.CajaMovimiento;
import com.hoteleria.quantum.entity.Estadia;
import com.hoteleria.quantum.entity.Pago;
import com.hoteleria.quantum.entity.Turno;
import com.hoteleria.quantum.entity.Usuario;
import com.hoteleria.quantum.entity.enums.EstadoEstadia;
import com.hoteleria.quantum.entity.enums.MetodoPago;
import com.hoteleria.quantum.entity.enums.TipoMovimientoCaja;
import com.hoteleria.quantum.repository.CajaMovimientoRepository;
import com.hoteleria.quantum.repository.EstadiaRepository;
import com.hoteleria.quantum.repository.PagoRepository;
import com.hoteleria.quantum.repository.TurnoRepository;
import com.hoteleria.quantum.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final EstadiaRepository estadiaRepository;
    private final TurnoRepository turnoRepository;
    private final CajaMovimientoRepository cajaMovimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<PagoResponse> findByEstadiaId(Long estadiaId) {
        List<Pago> pagos = pagoRepository.findByEstadiaId(estadiaId);
        // Force eager load of lazy relations within transaction
        pagos.forEach(this::initializeLazyRelations);
        return pagos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> findByTurnoId(Long turnoId) {
        List<Pago> pagos = pagoRepository.findByTurnoId(turnoId);
        pagos.forEach(this::initializeLazyRelations);
        return pagos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PagoResponse registrar(PagoRequest request, Long usuarioId) {
        // Validate estadia exists and is ACTIVA
        Estadia estadia = estadiaRepository.findById(request.getEstadiaId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Estadía no encontrada con id: " + request.getEstadiaId()));

        if (estadia.getEstado() != EstadoEstadia.ACTIVA) {
            throw new IllegalStateException(
                    "Solo se pueden registrar pagos para estadías con estado ACTIVA. Estado actual: "
                            + estadia.getEstado());
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + usuarioId));

        // Get active turno for the user
        Turno turno = turnoRepository.findActivoByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró un turno activo para el usuario. Debe iniciar un turno antes de registrar pagos."));

        MetodoPago metodoPago = MetodoPago.valueOf(request.getMetodoPago());

        // Create Pago
        Pago pago = Pago.builder()
                .estadia(estadia)
                .metodoPago(metodoPago)
                .monto(request.getMonto())
                .numeroOperacion(request.getNumeroOperacion())
                .concepto(request.getConcepto())
                .usuarioRegistro(usuario)
                .turno(turno)
                .build();
        pago = pagoRepository.save(pago);

        // Create CajaMovimiento (INGRESO)
        CajaMovimiento movimiento = CajaMovimiento.builder()
                .tipo(TipoMovimientoCaja.INGRESO)
                .metodoPago(metodoPago)
                .concepto(request.getConcepto())
                .monto(request.getMonto())
                .usuario(usuario)
                .turno(turno)
                .estadia(estadia)
                .pago(pago)
                .build();
        cajaMovimientoRepository.save(movimiento);

        // Access estadia.codigo for response within transaction
        String estadiaCodigo = estadia.getCodigo();

        auditService.registrarConDetalles(usuarioId, "REGISTRAR_PAGO", "Pago", pago.getId(),
                Map.of("estadiaId", estadia.getId(),
                        "estadiaCodigo", estadiaCodigo != null ? estadiaCodigo : "",
                        "monto", pago.getMonto().toString(),
                        "metodoPago", metodoPago.name()));

        log.info("Pago registrado: {} - Monto: {} - Estadía: {}",
                pago.getId(), pago.getMonto(), estadiaCodigo);

        return toResponse(pago, estadiaCodigo, usuario.getNombre());
    }

    @Transactional(readOnly = true)
    public BigDecimal getSumByEstadia(Long estadiaId) {
        return pagoRepository.sumMontoByEstadiaId(estadiaId);
    }

    /**
     * Internal method to register a payment. Used by EstadiaService during check-in/checkout.
     */
    @Transactional
    public Pago registrarInterno(Estadia estadia, MetodoPago metodoPago, BigDecimal monto,
                                 String concepto, String numeroOperacion, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + usuarioId));

        Turno turno = turnoRepository.findActivoByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró un turno activo para el usuario."));

        Pago pago = Pago.builder()
                .estadia(estadia)
                .metodoPago(metodoPago)
                .monto(monto)
                .numeroOperacion(numeroOperacion)
                .concepto(concepto)
                .usuarioRegistro(usuario)
                .turno(turno)
                .build();
        pago = pagoRepository.save(pago);

        CajaMovimiento movimiento = CajaMovimiento.builder()
                .tipo(TipoMovimientoCaja.INGRESO)
                .metodoPago(metodoPago)
                .concepto(concepto)
                .monto(monto)
                .usuario(usuario)
                .turno(turno)
                .estadia(estadia)
                .pago(pago)
                .build();
        cajaMovimientoRepository.save(movimiento);

        log.debug("Pago interno registrado: {} - Monto: {}", pago.getId(), monto);
        return pago;
    }

    private void initializeLazyRelations(Pago pago) {
        if (pago.getEstadia() != null) {
            pago.getEstadia().getCodigo();
        }
        if (pago.getUsuarioRegistro() != null) {
            pago.getUsuarioRegistro().getNombre();
        }
    }

    private PagoResponse toResponse(Pago pago) {
        return toResponse(pago,
                pago.getEstadia() != null ? pago.getEstadia().getCodigo() : null,
                pago.getUsuarioRegistro() != null ? pago.getUsuarioRegistro().getNombre() : null);
    }

    private PagoResponse toResponse(Pago pago, String estadiaCodigo, String usuarioNombre) {
        return PagoResponse.builder()
                .id(pago.getId())
                .estadiaId(pago.getEstadia() != null ? pago.getEstadia().getId() : null)
                .estadiaCodigo(estadiaCodigo)
                .metodoPago(pago.getMetodoPago() != null ? pago.getMetodoPago().name() : null)
                .monto(pago.getMonto())
                .numeroOperacion(pago.getNumeroOperacion())
                .concepto(pago.getConcepto())
                .usuarioRegistroId(pago.getUsuarioRegistro() != null ? pago.getUsuarioRegistro().getId() : null)
                .usuarioRegistroNombre(usuarioNombre)
                .turnoId(pago.getTurno() != null ? pago.getTurno().getId() : null)
                .fechaPago(pago.getFechaPago())
                .build();
    }
}
