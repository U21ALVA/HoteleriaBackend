package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.HabitacionEstadoRequest;
import com.hoteleria.quantum.dto.HabitacionRequest;
import com.hoteleria.quantum.dto.HabitacionResponse;
import com.hoteleria.quantum.entity.CategoriaHabitacion;
import com.hoteleria.quantum.entity.Habitacion;
import com.hoteleria.quantum.entity.HabitacionEstadoHistorial;
import com.hoteleria.quantum.entity.Usuario;
import com.hoteleria.quantum.entity.enums.EstadoHabitacion;
import com.hoteleria.quantum.mapper.HabitacionMapper;
import com.hoteleria.quantum.repository.CategoriaHabitacionRepository;
import com.hoteleria.quantum.repository.HabitacionEstadoHistorialRepository;
import com.hoteleria.quantum.repository.HabitacionRepository;
import com.hoteleria.quantum.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final CategoriaHabitacionRepository categoriaRepository;
    private final HabitacionEstadoHistorialRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final HabitacionMapper habitacionMapper;
    private final AuditService auditService;
    private final WebSocketEventService webSocketEventService;

    /**
     * Valid state transitions for rooms.
     * DISPONIBLE → OCUPADA is handled only internally via check-in.
     */
    private static final Map<EstadoHabitacion, Set<EstadoHabitacion>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = new EnumMap<>(EstadoHabitacion.class);
        VALID_TRANSITIONS.put(EstadoHabitacion.DISPONIBLE,
                Set.of(EstadoHabitacion.OCUPADA, EstadoHabitacion.MANTENIMIENTO, EstadoHabitacion.BLOQUEADA));
        VALID_TRANSITIONS.put(EstadoHabitacion.OCUPADA,
                Set.of(EstadoHabitacion.LIMPIEZA, EstadoHabitacion.MANTENIMIENTO));
        VALID_TRANSITIONS.put(EstadoHabitacion.LIMPIEZA,
                Set.of(EstadoHabitacion.DISPONIBLE));
        VALID_TRANSITIONS.put(EstadoHabitacion.MANTENIMIENTO,
                Set.of(EstadoHabitacion.DISPONIBLE, EstadoHabitacion.LIMPIEZA));
        VALID_TRANSITIONS.put(EstadoHabitacion.BLOQUEADA,
                Set.of(EstadoHabitacion.DISPONIBLE));
    }

    @Transactional(readOnly = true)
    public List<HabitacionResponse> findAll() {
        List<Habitacion> habitaciones = habitacionRepository.findByActivoTrue();
        // Access categoria eagerly within transaction to avoid LazyInitializationException
        habitaciones.forEach(h -> {
            if (h.getCategoria() != null) {
                h.getCategoria().getNombre(); // force initialization
            }
        });
        return habitacionMapper.toResponseList(habitaciones);
    }

    @Transactional(readOnly = true)
    public HabitacionResponse findById(Integer id) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Habitación no encontrada con id: " + id));
        // Force eager load of categoria
        if (habitacion.getCategoria() != null) {
            habitacion.getCategoria().getNombre();
        }
        return habitacionMapper.toResponse(habitacion);
    }

    @Transactional(readOnly = true)
    public List<HabitacionResponse> findByEstado(EstadoHabitacion estado) {
        List<Habitacion> habitaciones = habitacionRepository.findByEstadoAndActivoTrue(estado);
        habitaciones.forEach(h -> {
            if (h.getCategoria() != null) {
                h.getCategoria().getNombre();
            }
        });
        return habitacionMapper.toResponseList(habitaciones);
    }

    @Transactional
    public HabitacionResponse create(HabitacionRequest request) {
        CategoriaHabitacion categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Categoría no encontrada con id: " + request.getCategoriaId()));

        if (habitacionRepository.existsByNumero(request.getNumero())) {
            throw new IllegalArgumentException(
                    "Ya existe una habitación con el número: " + request.getNumero());
        }

        Habitacion habitacion = habitacionMapper.toEntity(request);
        habitacion.setCategoria(categoria);
        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
        habitacion.setActivo(true);
        habitacion = habitacionRepository.save(habitacion);

        log.info("Habitación creada: {} (id={})", habitacion.getNumero(), habitacion.getId());
        return habitacionMapper.toResponse(habitacion);
    }

    @Transactional
    public HabitacionResponse update(Integer id, HabitacionRequest request) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Habitación no encontrada con id: " + id));

        // Check numero uniqueness if changed
        if (!habitacion.getNumero().equals(request.getNumero())
                && habitacionRepository.existsByNumero(request.getNumero())) {
            throw new IllegalArgumentException(
                    "Ya existe una habitación con el número: " + request.getNumero());
        }

        CategoriaHabitacion categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Categoría no encontrada con id: " + request.getCategoriaId()));

        habitacion.setNumero(request.getNumero());
        habitacion.setPiso(request.getPiso());
        habitacion.setCategoria(categoria);
        habitacion.setNotas(request.getNotas());
        habitacion = habitacionRepository.save(habitacion);

        log.info("Habitación actualizada: {} (id={})", habitacion.getNumero(), habitacion.getId());
        return habitacionMapper.toResponse(habitacion);
    }

    @Transactional
    public HabitacionResponse cambiarEstado(Integer id, HabitacionEstadoRequest request, Long usuarioId) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Habitación no encontrada con id: " + id));

        EstadoHabitacion nuevoEstado = EstadoHabitacion.valueOf(request.getEstado());
        EstadoHabitacion estadoActual = habitacion.getEstado();

        validateTransition(estadoActual, nuevoEstado);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + usuarioId));

        // Save state history
        HabitacionEstadoHistorial historial = HabitacionEstadoHistorial.builder()
                .habitacion(habitacion)
                .estadoAnterior(estadoActual)
                .estadoNuevo(nuevoEstado)
                .usuario(usuario)
                .motivo(request.getMotivo())
                .build();
        historialRepository.save(historial);

        habitacion.setEstado(nuevoEstado);
        habitacion = habitacionRepository.save(habitacion);

        // Force categoria load for response
        if (habitacion.getCategoria() != null) {
            habitacion.getCategoria().getNombre();
        }

        auditService.registrarConDetalles(usuarioId, "CAMBIO_ESTADO_HABITACION", "Habitacion",
                habitacion.getId().longValue(),
                Map.of("habitacionNumero", habitacion.getNumero(),
                        "estadoAnterior", estadoActual.name(),
                        "estadoNuevo", nuevoEstado.name(),
                        "motivo", request.getMotivo() != null ? request.getMotivo() : ""));

        webSocketEventService.notificarCambioHabitacion(habitacion.getId(), habitacion.getNumero(),
                estadoActual.name(), nuevoEstado.name());

        log.info("Habitación {} cambió de estado: {} → {}", habitacion.getNumero(),
                estadoActual, nuevoEstado);
        return habitacionMapper.toResponse(habitacion);
    }

    /**
     * Internal method to change room state without external validation.
     * Used by EstadiaService during check-in, checkout, etc.
     */
    @Transactional
    public void cambiarEstadoInterno(Integer habitacionId, EstadoHabitacion nuevoEstado,
                                     Long usuarioId, String motivo) {
        Habitacion habitacion = habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Habitación no encontrada con id: " + habitacionId));

        EstadoHabitacion estadoActual = habitacion.getEstado();

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

        HabitacionEstadoHistorial historial = HabitacionEstadoHistorial.builder()
                .habitacion(habitacion)
                .estadoAnterior(estadoActual)
                .estadoNuevo(nuevoEstado)
                .usuario(usuario)
                .motivo(motivo)
                .build();
        historialRepository.save(historial);

        habitacion.setEstado(nuevoEstado);
        habitacionRepository.save(habitacion);

        webSocketEventService.notificarCambioHabitacion(habitacion.getId(), habitacion.getNumero(),
                estadoActual.name(), nuevoEstado.name());

        log.debug("Habitación {} estado interno: {} → {} ({})",
                habitacion.getNumero(), estadoActual, nuevoEstado, motivo);
    }

    @Transactional
    public void deactivate(Integer id) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Habitación no encontrada con id: " + id));

        habitacion.setActivo(false);
        habitacionRepository.save(habitacion);
        log.info("Habitación desactivada: {} (id={})", habitacion.getNumero(), habitacion.getId());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOcupacionStats() {
        Map<String, Object> stats = new HashMap<>();
        for (EstadoHabitacion estado : EstadoHabitacion.values()) {
            stats.put(estado.name(), habitacionRepository.countByEstado(estado));
        }
        long total = habitacionRepository.findByActivoTrue().size();
        stats.put("TOTAL", total);
        return stats;
    }

    private void validateTransition(EstadoHabitacion from, EstadoHabitacion to) {
        Set<EstadoHabitacion> allowed = VALID_TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new IllegalStateException(
                    String.format("Transición de estado inválida: %s → %s", from, to));
        }
    }
}
