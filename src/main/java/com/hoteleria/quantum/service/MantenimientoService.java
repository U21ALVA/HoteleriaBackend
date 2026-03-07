package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.MantenimientoRequest;
import com.hoteleria.quantum.dto.MantenimientoResolverRequest;
import com.hoteleria.quantum.dto.MantenimientoResponse;
import com.hoteleria.quantum.entity.Habitacion;
import com.hoteleria.quantum.entity.Mantenimiento;
import com.hoteleria.quantum.entity.Usuario;
import com.hoteleria.quantum.entity.enums.EstadoHabitacion;
import com.hoteleria.quantum.entity.enums.TipoMantenimiento;
import com.hoteleria.quantum.mapper.MantenimientoMapper;
import com.hoteleria.quantum.repository.HabitacionRepository;
import com.hoteleria.quantum.repository.MantenimientoRepository;
import com.hoteleria.quantum.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MantenimientoService {

    private final MantenimientoRepository mantenimientoRepository;
    private final MantenimientoMapper mantenimientoMapper;
    private final HabitacionRepository habitacionRepository;
    private final HabitacionService habitacionService;
    private final AuditService auditService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public MantenimientoResponse reportar(MantenimientoRequest request, Long usuarioId) {
        Habitacion habitacion = habitacionRepository.findById(request.getHabitacionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Habitación no encontrada con id: " + request.getHabitacionId()));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + usuarioId));

        Mantenimiento mantenimiento = Mantenimiento.builder()
                .habitacion(habitacion)
                .tipo(TipoMantenimiento.valueOf(request.getTipo()))
                .usuarioReporta(usuario)
                .descripcion(request.getDescripcion())
                .resuelto(false)
                .fechaProgramada(request.getFechaProgramada())
                .build();

        mantenimiento = mantenimientoRepository.save(mantenimiento);

        // Change habitacion estado to MANTENIMIENTO
        habitacionService.cambiarEstadoInterno(habitacion.getId(), EstadoHabitacion.MANTENIMIENTO,
                usuarioId, "Mantenimiento reportado: " + request.getDescripcion());

        auditService.registrarConDetalles(usuarioId, "REPORTAR_MANTENIMIENTO", "Mantenimiento",
                mantenimiento.getId(),
                Map.of("habitacionId", request.getHabitacionId(),
                        "habitacionNumero", habitacion.getNumero(),
                        "tipo", request.getTipo(),
                        "descripcion", request.getDescripcion()));

        log.info("Mantenimiento reportado: id={} habitacion={}",
                mantenimiento.getId(), habitacion.getNumero());
        return mantenimientoMapper.toResponse(mantenimiento);
    }

    @Transactional
    public MantenimientoResponse resolver(Long id, MantenimientoResolverRequest request, Long usuarioId) {
        Mantenimiento mantenimiento = mantenimientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Mantenimiento no encontrado con id: " + id));

        if (mantenimiento.getResuelto()) {
            throw new IllegalStateException("El mantenimiento ya fue resuelto");
        }

        mantenimiento.setResuelto(true);
        mantenimiento.setFechaResolucion(LocalDateTime.now());
        mantenimiento.setNotasResolucion(request.getNotasResolucion());
        mantenimiento = mantenimientoRepository.save(mantenimiento);

        // Change habitacion estado to DISPONIBLE
        Habitacion habitacion = mantenimiento.getHabitacion();
        habitacionService.cambiarEstadoInterno(habitacion.getId(), EstadoHabitacion.DISPONIBLE,
                usuarioId, "Mantenimiento resuelto: " + (request.getNotasResolucion() != null
                        ? request.getNotasResolucion() : "Sin notas"));

        auditService.registrarConDetalles(usuarioId, "RESOLVER_MANTENIMIENTO", "Mantenimiento",
                mantenimiento.getId(),
                Map.of("habitacionId", habitacion.getId(),
                        "habitacionNumero", habitacion.getNumero(),
                        "notasResolucion", request.getNotasResolucion() != null
                                ? request.getNotasResolucion() : ""));

        log.info("Mantenimiento resuelto: id={} habitacion={}",
                mantenimiento.getId(), habitacion.getNumero());
        return mantenimientoMapper.toResponse(mantenimiento);
    }

    @Transactional(readOnly = true)
    public List<MantenimientoResponse> findPendientes() {
        List<Mantenimiento> mantenimientos = mantenimientoRepository.findByResueltoFalseOrderByFechaReporteDesc();
        mantenimientos.forEach(this::forceEagerLoad);
        return mantenimientoMapper.toResponseList(mantenimientos);
    }

    @Transactional(readOnly = true)
    public List<MantenimientoResponse> findByHabitacionId(Integer habitacionId) {
        List<Mantenimiento> mantenimientos = mantenimientoRepository.findByHabitacionId(habitacionId);
        mantenimientos.forEach(this::forceEagerLoad);
        return mantenimientoMapper.toResponseList(mantenimientos);
    }

    @Transactional(readOnly = true)
    public List<MantenimientoResponse> findAll() {
        List<Mantenimiento> mantenimientos = mantenimientoRepository.findAll();
        mantenimientos.forEach(this::forceEagerLoad);
        return mantenimientoMapper.toResponseList(mantenimientos);
    }

    private void forceEagerLoad(Mantenimiento m) {
        if (m.getHabitacion() != null) {
            m.getHabitacion().getNumero();
        }
        if (m.getUsuarioReporta() != null) {
            m.getUsuarioReporta().getNombre();
        }
    }
}
