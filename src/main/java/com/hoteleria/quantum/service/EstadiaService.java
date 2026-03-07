package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.CheckinRequest;
import com.hoteleria.quantum.dto.CheckoutRequest;
import com.hoteleria.quantum.dto.EstadiaCreateRequest;
import com.hoteleria.quantum.dto.EstadiaHabitacionResponse;
import com.hoteleria.quantum.dto.EstadiaResponse;
import com.hoteleria.quantum.dto.PagoResponse;
import com.hoteleria.quantum.entity.CategoriaHabitacion;
import com.hoteleria.quantum.entity.Estadia;
import com.hoteleria.quantum.entity.EstadiaHabitacion;
import com.hoteleria.quantum.entity.Habitacion;
import com.hoteleria.quantum.entity.Huesped;
import com.hoteleria.quantum.entity.Pago;
import com.hoteleria.quantum.entity.TarifaTemporada;
import com.hoteleria.quantum.entity.Usuario;
import com.hoteleria.quantum.entity.enums.EstadoEstadia;
import com.hoteleria.quantum.entity.enums.EstadoHabitacion;
import com.hoteleria.quantum.entity.enums.MetodoPago;
import com.hoteleria.quantum.entity.enums.OrigenEstadia;
import com.hoteleria.quantum.repository.EstadiaHabitacionRepository;
import com.hoteleria.quantum.repository.EstadiaRepository;
import com.hoteleria.quantum.repository.HabitacionRepository;
import com.hoteleria.quantum.repository.HuespedRepository;
import com.hoteleria.quantum.repository.PagoRepository;
import com.hoteleria.quantum.repository.TarifaTemporadaRepository;
import com.hoteleria.quantum.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstadiaService {

    private final EstadiaRepository estadiaRepository;
    private final EstadiaHabitacionRepository estadiaHabitacionRepository;
    private final HabitacionRepository habitacionRepository;
    private final HuespedRepository huespedRepository;
    private final PagoRepository pagoRepository;
    private final TarifaTemporadaRepository tarifaTemporadaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HuespedService huespedService;
    private final HabitacionService habitacionService;
    private final PagoService pagoService;
    private final ConfiguracionService configuracionService;
    private final AuditService auditService;
    private final WebSocketEventService webSocketEventService;
    private final EntityManager entityManager;

    // ==================== QUERY METHODS ====================

    @Transactional(readOnly = true)
    public List<EstadiaResponse> findAll(EstadoEstadia estado) {
        List<Estadia> estadias;
        if (estado != null) {
            estadias = estadiaRepository.findByEstado(estado);
        } else {
            // Return all non-cancelled
            estadias = estadiaRepository.findByEstadoIn(
                    List.of(EstadoEstadia.REGISTRADA, EstadoEstadia.ACTIVA, EstadoEstadia.COMPLETADA));
        }
        return estadias.stream()
                .map(this::buildFullResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EstadiaResponse findById(Long id) {
        Estadia estadia = estadiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Estadía no encontrada con id: " + id));
        return buildFullResponse(estadia);
    }

    @Transactional(readOnly = true)
    public EstadiaResponse findByCodigo(String codigo) {
        Estadia estadia = estadiaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Estadía no encontrada con código: " + codigo));
        return buildFullResponse(estadia);
    }

    // ==================== REGISTRAR (CREATE) ====================

    @Transactional
    public EstadiaResponse registrar(EstadiaCreateRequest request, Long usuarioId) {
        // 1. Resolve huésped
        Huesped huesped;
        if (request.getHuespedId() != null) {
            huesped = huespedRepository.findById(request.getHuespedId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Huésped no encontrado con id: " + request.getHuespedId()));
        } else if (request.getHuesped() != null) {
            huesped = huespedService.findOrCreate(request.getHuesped());
        } else {
            throw new IllegalArgumentException(
                    "Debe proporcionar huespedId o datos del huésped");
        }

        // 2. Validate ALL rooms exist and are DISPONIBLE
        List<Habitacion> habitaciones = new ArrayList<>();
        for (Integer habitacionId : request.getHabitacionIds()) {
            Habitacion habitacion = habitacionRepository.findById(habitacionId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Habitación no encontrada con id: " + habitacionId));

            if (habitacion.getEstado() != EstadoHabitacion.DISPONIBLE) {
                throw new IllegalArgumentException(
                        "La habitación " + habitacion.getNumero() + " no está disponible. Estado actual: "
                                + habitacion.getEstado());
            }
            habitaciones.add(habitacion);
        }

        // 3. Calculate tarifa vigente for each room
        LocalDate fechaReferencia = LocalDate.now();
        Integer noches = request.getNoches();

        List<BigDecimal> tarifas = new ArrayList<>();
        for (Habitacion habitacion : habitaciones) {
            // Force eager load of categoria
            CategoriaHabitacion categoria = habitacion.getCategoria();
            if (categoria != null) {
                categoria.getNombre();
            }
            BigDecimal tarifa = calcularTarifaVigente(habitacion.getId(), fechaReferencia);
            tarifas.add(tarifa);
        }

        // 4. Calculate precio_total = sum(tarifa × noches)
        BigDecimal precioTotal = BigDecimal.ZERO;
        for (BigDecimal tarifa : tarifas) {
            precioTotal = precioTotal.add(tarifa.multiply(BigDecimal.valueOf(noches)));
        }

        // 5. Calculate anticipo_requerido
        BigDecimal anticipoPorcentaje;
        try {
            anticipoPorcentaje = configuracionService.getValorDecimal("anticipo_porcentaje_minimo");
        } catch (EntityNotFoundException e) {
            anticipoPorcentaje = new BigDecimal("30"); // default 30%
        }
        BigDecimal anticipoRequerido = precioTotal.multiply(anticipoPorcentaje)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // 6. Depósito de garantía
        BigDecimal depositoGarantia = request.getDepositoGarantia();
        if (depositoGarantia == null || depositoGarantia.compareTo(BigDecimal.ZERO) == 0) {
            try {
                depositoGarantia = configuracionService.getValorDecimal("deposito_garantia_default");
            } catch (EntityNotFoundException e) {
                depositoGarantia = BigDecimal.ZERO;
            }
        }

        // 7-8. Determine estado based on origen
        OrigenEstadia origen = OrigenEstadia.valueOf(request.getOrigen());
        EstadoEstadia estadoInicial;
        LocalDateTime ahora = LocalDateTime.now();

        if (origen == OrigenEstadia.PRESENCIAL) {
            estadoInicial = EstadoEstadia.ACTIVA;
        } else {
            estadoInicial = EstadoEstadia.REGISTRADA;
        }

        // Resolve usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + usuarioId));

        // 10. Save Estadia
        Estadia estadia = Estadia.builder()
                .huesped(huesped)
                .usuarioRegistro(usuario)
                .origen(origen)
                .fechaRegistro(ahora)
                .fechaCheckin(origen == OrigenEstadia.PRESENCIAL ? ahora : null)
                .fechaCheckoutEstimado(request.getFechaCheckoutEstimado())
                .estado(estadoInicial)
                .precioTotal(precioTotal)
                .depositoGarantia(depositoGarantia)
                .depositoDevuelto(false)
                .anticipoRequerido(anticipoRequerido)
                .penalizacion(BigDecimal.ZERO)
                .notas(request.getNotas())
                .build();
        estadia = estadiaRepository.save(estadia);

        // Refresh to pick up DB-generated 'codigo' from trigger
        entityManager.flush();
        entityManager.refresh(estadia);

        // 11. Save EstadiaHabitacion records and change room state if PRESENCIAL
        for (int i = 0; i < habitaciones.size(); i++) {
            Habitacion habitacion = habitaciones.get(i);
            BigDecimal tarifa = tarifas.get(i);
            BigDecimal subtotal = tarifa.multiply(BigDecimal.valueOf(noches));

            EstadiaHabitacion eh = EstadiaHabitacion.builder()
                    .estadia(estadia)
                    .habitacion(habitacion)
                    .precioNoche(tarifa)
                    .noches(noches)
                    .subtotal(subtotal)
                    .build();
            estadiaHabitacionRepository.save(eh);

            // If PRESENCIAL, rooms go to OCUPADA immediately
            if (origen == OrigenEstadia.PRESENCIAL) {
                habitacionService.cambiarEstadoInterno(habitacion.getId(),
                        EstadoHabitacion.OCUPADA, usuarioId, "Check-in presencial - Estadía registrada");
            }
        }

        // 12. Audit log
        auditService.registrarConDetalles(usuarioId, "REGISTRAR_ESTADIA", "Estadia", estadia.getId(),
                Map.of("origen", origen.name(),
                        "estado", estadoInicial.name(),
                        "huespedId", huesped.getId(),
                        "huespedNombre", huesped.getNombreCompleto(),
                        "precioTotal", precioTotal.toString(),
                        "habitaciones", request.getHabitacionIds().toString()));

        log.info("Estadía registrada: id={}, origen={}, estado={}, huésped={}",
                estadia.getId(), origen, estadoInicial, huesped.getNombreCompleto());

        webSocketEventService.notificarCambioEstadia(estadia.getId(), estadia.getCodigo(),
                "NUEVA_ESTADIA", estadoInicial.name());

        // 13. Return response
        return buildFullResponse(estadia);
    }

    // ==================== CHECK-IN ====================

    @Transactional
    public EstadiaResponse checkin(Long estadiaId, CheckinRequest request, Long usuarioId) {
        // 1. Find estadia and verify state
        Estadia estadia = estadiaRepository.findById(estadiaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Estadía no encontrada con id: " + estadiaId));

        if (estadia.getEstado() != EstadoEstadia.REGISTRADA) {
            throw new IllegalStateException(
                    "Solo se puede hacer check-in de estadías con estado REGISTRADA. Estado actual: "
                            + estadia.getEstado());
        }

        // Validate anticipo meets minimum
        if (request.getAnticipoMonto().compareTo(estadia.getAnticipoRequerido()) < 0) {
            throw new IllegalArgumentException(
                    String.format("El anticipo (%.2f) es menor al mínimo requerido (%.2f)",
                            request.getAnticipoMonto(), estadia.getAnticipoRequerido()));
        }

        LocalDateTime ahora = LocalDateTime.now();

        // 2. Update estadia state
        estadia.setFechaCheckin(ahora);
        estadia.setEstado(EstadoEstadia.ACTIVA);

        // Update deposito if provided
        if (request.getDepositoGarantia() != null) {
            estadia.setDepositoGarantia(request.getDepositoGarantia());
        }

        estadia = estadiaRepository.save(estadia);

        // 3. Change all associated rooms to OCUPADA
        List<EstadiaHabitacion> estadiaHabitaciones = estadiaHabitacionRepository
                .findByEstadiaId(estadiaId);
        for (EstadiaHabitacion eh : estadiaHabitaciones) {
            habitacionService.cambiarEstadoInterno(eh.getHabitacion().getId(),
                    EstadoHabitacion.OCUPADA, usuarioId, "Check-in - Estadía " + estadia.getId());
        }

        // 4. Register anticipo payment
        MetodoPago metodoPago = MetodoPago.valueOf(request.getAnticipoMetodoPago());
        pagoService.registrarInterno(estadia, metodoPago, request.getAnticipoMonto(),
                "Anticipo de estadía", request.getNumeroOperacion(), usuarioId);

        // 5. Audit log
        auditService.registrarConDetalles(usuarioId, "CHECKIN", "Estadia", estadia.getId(),
                Map.of("anticipo", request.getAnticipoMonto().toString(),
                        "metodoPago", metodoPago.name()));

        log.info("Check-in realizado: estadía id={}, anticipo={}",
                estadia.getId(), request.getAnticipoMonto());

        webSocketEventService.notificarCambioEstadia(estadia.getId(), estadia.getCodigo(),
                "CHECKIN", EstadoEstadia.ACTIVA.name());

        // 6. Return updated response
        return buildFullResponse(estadia);
    }

    // ==================== CHECK-OUT ====================

    @Transactional
    public EstadiaResponse checkout(Long estadiaId, CheckoutRequest request, Long usuarioId) {
        // 1. Find estadia and verify state
        Estadia estadia = estadiaRepository.findById(estadiaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Estadía no encontrada con id: " + estadiaId));

        if (estadia.getEstado() != EstadoEstadia.ACTIVA) {
            throw new IllegalStateException(
                    "Solo se puede hacer checkout de estadías con estado ACTIVA. Estado actual: "
                            + estadia.getEstado());
        }

        LocalDateTime ahora = LocalDateTime.now();

        // 2. Calculate late checkout penalty
        if (estadia.getFechaCheckoutEstimado() != null
                && ahora.isAfter(estadia.getFechaCheckoutEstimado())) {
            long horasExtra = ChronoUnit.HOURS.between(estadia.getFechaCheckoutEstimado(), ahora);
            if (horasExtra < 1) {
                horasExtra = 1; // minimum 1 hour charge
            }

            BigDecimal cargoHoraExtra;
            try {
                cargoHoraExtra = configuracionService.getValorDecimal("cargo_hora_extra");
            } catch (EntityNotFoundException e) {
                cargoHoraExtra = new BigDecimal("20"); // default
            }

            BigDecimal penalizacion = cargoHoraExtra.multiply(BigDecimal.valueOf(horasExtra));
            estadia.setPenalizacion(penalizacion);

            log.info("Checkout tardío: {} horas extra, penalización: {}",
                    horasExtra, penalizacion);
        }

        // 3. Calculate saldo pendiente
        BigDecimal totalPagos = pagoRepository.sumMontoByEstadiaId(estadiaId);
        BigDecimal saldoPendiente = estadia.getPrecioTotal()
                .add(estadia.getPenalizacion() != null ? estadia.getPenalizacion() : BigDecimal.ZERO)
                .subtract(totalPagos);

        // 4. Register final payment if provided
        if (saldoPendiente.compareTo(BigDecimal.ZERO) > 0 && request.getMontoFinal() != null
                && request.getMontoFinal().compareTo(BigDecimal.ZERO) > 0) {
            MetodoPago metodoPago = request.getMetodoPago() != null
                    ? MetodoPago.valueOf(request.getMetodoPago())
                    : MetodoPago.EFECTIVO;
            pagoService.registrarInterno(estadia, metodoPago, request.getMontoFinal(),
                    "Pago final de checkout", request.getNumeroOperacion(), usuarioId);
        }

        // 5. Handle deposit return
        if (Boolean.TRUE.equals(request.getDevolverDeposito())) {
            estadia.setDepositoDevuelto(true);
        }

        // 6. Update estadia
        estadia.setFechaCheckoutReal(ahora);
        estadia.setEstado(EstadoEstadia.COMPLETADA);
        estadia = estadiaRepository.save(estadia);

        // 7. Change all associated rooms to LIMPIEZA
        List<EstadiaHabitacion> estadiaHabitaciones = estadiaHabitacionRepository
                .findByEstadiaId(estadiaId);
        for (EstadiaHabitacion eh : estadiaHabitaciones) {
            habitacionService.cambiarEstadoInterno(eh.getHabitacion().getId(),
                    EstadoHabitacion.LIMPIEZA, usuarioId, "Checkout - Estadía " + estadia.getId());
        }

        // 8. Audit log
        auditService.registrarConDetalles(usuarioId, "CHECKOUT", "Estadia", estadia.getId(),
                Map.of("penalizacion", estadia.getPenalizacion() != null
                                ? estadia.getPenalizacion().toString() : "0",
                        "depositoDevuelto", String.valueOf(estadia.getDepositoDevuelto())));

        log.info("Checkout realizado: estadía id={}", estadia.getId());

        webSocketEventService.notificarCambioEstadia(estadia.getId(), estadia.getCodigo(),
                "CHECKOUT", EstadoEstadia.COMPLETADA.name());

        // 9. Return response
        return buildFullResponse(estadia);
    }

    // ==================== CANCELAR ====================

    @Transactional
    public EstadiaResponse cancelar(Long estadiaId, Long usuarioId) {
        // 1. Find estadia and verify state
        Estadia estadia = estadiaRepository.findById(estadiaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Estadía no encontrada con id: " + estadiaId));

        if (estadia.getEstado() != EstadoEstadia.REGISTRADA
                && estadia.getEstado() != EstadoEstadia.ACTIVA) {
            throw new IllegalStateException(
                    "Solo se pueden cancelar estadías con estado REGISTRADA o ACTIVA. Estado actual: "
                            + estadia.getEstado());
        }

        // 2. Apply penalty based on config (if applicable)
        BigDecimal penalizacionCancelacion;
        try {
            penalizacionCancelacion = configuracionService.getValorDecimal("penalizacion_cancelacion");
        } catch (EntityNotFoundException e) {
            penalizacionCancelacion = BigDecimal.ZERO;
        }
        estadia.setPenalizacion(penalizacionCancelacion);

        // 3. Set estado = CANCELADA
        EstadoEstadia estadoAnterior = estadia.getEstado();
        estadia.setEstado(EstadoEstadia.CANCELADA);
        estadia = estadiaRepository.save(estadia);

        // 4. If rooms were OCUPADA (estadía was ACTIVA), change to LIMPIEZA
        List<EstadiaHabitacion> estadiaHabitaciones = estadiaHabitacionRepository
                .findByEstadiaId(estadiaId);
        if (estadoAnterior == EstadoEstadia.ACTIVA) {
            for (EstadiaHabitacion eh : estadiaHabitaciones) {
                Habitacion habitacion = eh.getHabitacion();
                if (habitacion.getEstado() == EstadoHabitacion.OCUPADA) {
                    habitacionService.cambiarEstadoInterno(habitacion.getId(),
                            EstadoHabitacion.LIMPIEZA, usuarioId,
                            "Cancelación de estadía " + estadia.getId());
                }
            }
        }

        // 5. Audit log
        auditService.registrarConDetalles(usuarioId, "CANCELAR_ESTADIA", "Estadia", estadia.getId(),
                Map.of("estadoAnterior", estadoAnterior.name(),
                        "penalizacion", penalizacionCancelacion.toString()));

        log.info("Estadía cancelada: id={}, estadoAnterior={}", estadia.getId(), estadoAnterior);

        webSocketEventService.notificarCambioEstadia(estadia.getId(), estadia.getCodigo(),
                "CANCELAR", EstadoEstadia.CANCELADA.name());

        return buildFullResponse(estadia);
    }

    // ==================== EXTENDER ====================

    @Transactional
    public EstadiaResponse extender(Long estadiaId, LocalDateTime nuevaFechaCheckout,
                                     Integer nochesExtra, Long usuarioId) {
        // 1. Verify estadia is ACTIVA
        Estadia estadia = estadiaRepository.findById(estadiaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Estadía no encontrada con id: " + estadiaId));

        if (estadia.getEstado() != EstadoEstadia.ACTIVA) {
            throw new IllegalStateException(
                    "Solo se pueden extender estadías con estado ACTIVA. Estado actual: "
                            + estadia.getEstado());
        }

        // 2. Check rooms don't have conflicting estadias for extended period
        List<EstadiaHabitacion> estadiaHabitaciones = estadiaHabitacionRepository
                .findByEstadiaId(estadiaId);
        for (EstadiaHabitacion eh : estadiaHabitaciones) {
            Integer habitacionId = eh.getHabitacion().getId();
            List<Estadia> conflictos = estadiaRepository.findActiveByHabitacionId(habitacionId);
            // Filter out current estadia
            conflictos = conflictos.stream()
                    .filter(e -> !e.getId().equals(estadiaId))
                    .collect(Collectors.toList());

            for (Estadia conflicto : conflictos) {
                if (conflicto.getFechaCheckoutEstimado() != null
                        && nuevaFechaCheckout.isAfter(conflicto.getFechaCheckin() != null
                        ? conflicto.getFechaCheckin()
                        : conflicto.getFechaRegistro())) {
                    throw new IllegalArgumentException(
                            "La habitación " + eh.getHabitacion().getNumero()
                                    + " tiene una estadía conflictiva en el período extendido");
                }
            }
        }

        // 3. Recalculate precio with tarifa vigente for extra nights
        LocalDate fechaReferencia = LocalDate.now();
        BigDecimal precioExtra = BigDecimal.ZERO;
        for (EstadiaHabitacion eh : estadiaHabitaciones) {
            Habitacion habitacion = eh.getHabitacion();
            BigDecimal tarifa = calcularTarifaVigente(habitacion.getId(), fechaReferencia);
            BigDecimal subtotalExtra = tarifa.multiply(BigDecimal.valueOf(nochesExtra));
            precioExtra = precioExtra.add(subtotalExtra);

            // Update noches and subtotal on EstadiaHabitacion
            eh.setNoches(eh.getNoches() + nochesExtra);
            eh.setSubtotal(eh.getPrecioNoche().multiply(BigDecimal.valueOf(eh.getNoches())));
            estadiaHabitacionRepository.save(eh);
        }

        // 4. Update estadia
        estadia.setFechaCheckoutEstimado(nuevaFechaCheckout);
        estadia.setPrecioTotal(estadia.getPrecioTotal().add(precioExtra));
        estadia = estadiaRepository.save(estadia);

        // Audit log
        auditService.registrarConDetalles(usuarioId, "EXTENDER_ESTADIA", "Estadia", estadia.getId(),
                Map.of("nochesExtra", nochesExtra,
                        "precioExtra", precioExtra.toString(),
                        "nuevaFechaCheckout", nuevaFechaCheckout.toString()));

        log.info("Estadía extendida: id={}, nochesExtra={}, precioExtra={}",
                estadia.getId(), nochesExtra, precioExtra);

        webSocketEventService.notificarCambioEstadia(estadia.getId(), estadia.getCodigo(),
                "EXTENDER", EstadoEstadia.ACTIVA.name());

        return buildFullResponse(estadia);
    }

    // ==================== PRIVATE HELPERS ====================

    /**
     * Calculates the current rate for a room on a given date.
     * Checks TarifaTemporada first, falls back to CategoriaHabitacion.precioBase.
     */
    private BigDecimal calcularTarifaVigente(Integer habitacionId, LocalDate fecha) {
        Habitacion habitacion = habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Habitación no encontrada con id: " + habitacionId));

        CategoriaHabitacion categoria = habitacion.getCategoria();
        if (categoria == null) {
            throw new IllegalStateException(
                    "La habitación " + habitacion.getNumero() + " no tiene categoría asignada");
        }

        // Check for active seasonal rate
        List<TarifaTemporada> tarifasTemporada = tarifaTemporadaRepository
                .findActiveByCategoriaIdAndFecha(categoria.getId(), fecha);

        if (!tarifasTemporada.isEmpty()) {
            // Use the first matching seasonal rate
            BigDecimal precioTemporada = tarifasTemporada.get(0).getPrecioModificado();
            log.debug("Tarifa temporada aplicada para habitación {}: {} ({})",
                    habitacion.getNumero(), precioTemporada, tarifasTemporada.get(0).getNombre());
            return precioTemporada;
        }

        // Fallback to base price
        return categoria.getPrecioBase();
    }

    /**
     * Calculates the pending balance for an estadia.
     */
    private BigDecimal calcularSaldoPendiente(Long estadiaId, BigDecimal precioTotal,
                                              BigDecimal penalizacion) {
        BigDecimal totalPagos = pagoRepository.sumMontoByEstadiaId(estadiaId);
        BigDecimal pen = penalizacion != null ? penalizacion : BigDecimal.ZERO;
        return precioTotal.add(pen).subtract(totalPagos);
    }

    /**
     * Builds a complete EstadiaResponse with habitaciones, pagos, and saldoPendiente.
     * Must be called within a @Transactional context.
     */
    private EstadiaResponse buildFullResponse(Estadia estadia) {
        // Force eager load of lazy relations
        Huesped huesped = estadia.getHuesped();
        String huespedNombre = null;
        String huespedDocumento = null;
        Long huespedId = null;
        if (huesped != null) {
            huespedId = huesped.getId();
            huespedNombre = huesped.getNombreCompleto();
            huespedDocumento = huesped.getDocumentoIdentidad();
        }

        Usuario usuarioRegistro = estadia.getUsuarioRegistro();
        Long usuarioRegistroId = null;
        String usuarioRegistroNombre = null;
        if (usuarioRegistro != null) {
            usuarioRegistroId = usuarioRegistro.getId();
            usuarioRegistroNombre = usuarioRegistro.getNombre();
        }

        // Load habitaciones
        List<EstadiaHabitacion> estadiaHabitaciones = estadiaHabitacionRepository
                .findByEstadiaId(estadia.getId());

        List<EstadiaHabitacionResponse> habitacionesResponse = estadiaHabitaciones.stream()
                .map(eh -> {
                    Habitacion hab = eh.getHabitacion();
                    CategoriaHabitacion cat = hab.getCategoria();
                    return EstadiaHabitacionResponse.builder()
                            .id(eh.getId())
                            .habitacionId(hab.getId())
                            .habitacionNumero(hab.getNumero())
                            .habitacionPiso(hab.getPiso())
                            .categoriaNombre(cat != null ? cat.getNombre() : null)
                            .precioNoche(eh.getPrecioNoche())
                            .noches(eh.getNoches())
                            .subtotal(eh.getSubtotal())
                            .build();
                })
                .collect(Collectors.toList());

        // Load pagos
        List<Pago> pagos = pagoRepository.findByEstadiaId(estadia.getId());
        List<PagoResponse> pagosResponse = pagos.stream()
                .map(pago -> {
                    String pagoUsuarioNombre = null;
                    if (pago.getUsuarioRegistro() != null) {
                        pagoUsuarioNombre = pago.getUsuarioRegistro().getNombre();
                    }
                    return PagoResponse.builder()
                            .id(pago.getId())
                            .estadiaId(estadia.getId())
                            .estadiaCodigo(estadia.getCodigo())
                            .metodoPago(pago.getMetodoPago() != null ? pago.getMetodoPago().name() : null)
                            .monto(pago.getMonto())
                            .numeroOperacion(pago.getNumeroOperacion())
                            .concepto(pago.getConcepto())
                            .usuarioRegistroId(pago.getUsuarioRegistro() != null
                                    ? pago.getUsuarioRegistro().getId() : null)
                            .usuarioRegistroNombre(pagoUsuarioNombre)
                            .turnoId(pago.getTurno() != null ? pago.getTurno().getId() : null)
                            .fechaPago(pago.getFechaPago())
                            .build();
                })
                .collect(Collectors.toList());

        // Calculate saldo pendiente
        BigDecimal saldoPendiente = calcularSaldoPendiente(estadia.getId(),
                estadia.getPrecioTotal(),
                estadia.getPenalizacion());

        return EstadiaResponse.builder()
                .id(estadia.getId())
                .codigo(estadia.getCodigo())
                .estado(estadia.getEstado() != null ? estadia.getEstado().name() : null)
                .origen(estadia.getOrigen() != null ? estadia.getOrigen().name() : null)
                .huespedId(huespedId)
                .huespedNombre(huespedNombre)
                .huespedDocumento(huespedDocumento)
                .usuarioRegistroId(usuarioRegistroId)
                .usuarioRegistroNombre(usuarioRegistroNombre)
                .fechaRegistro(estadia.getFechaRegistro())
                .fechaCheckin(estadia.getFechaCheckin())
                .fechaCheckoutEstimado(estadia.getFechaCheckoutEstimado())
                .fechaCheckoutReal(estadia.getFechaCheckoutReal())
                .precioTotal(estadia.getPrecioTotal())
                .depositoGarantia(estadia.getDepositoGarantia())
                .depositoDevuelto(estadia.getDepositoDevuelto() != null
                        && estadia.getDepositoDevuelto()
                        ? estadia.getDepositoGarantia() : BigDecimal.ZERO)
                .anticipoRequerido(estadia.getAnticipoRequerido())
                .penalizacion(estadia.getPenalizacion())
                .notas(estadia.getNotas())
                .habitaciones(habitacionesResponse)
                .pagos(pagosResponse)
                .saldoPendiente(saldoPendiente)
                .creadoEn(estadia.getCreadoEn())
                .build();
    }
}
