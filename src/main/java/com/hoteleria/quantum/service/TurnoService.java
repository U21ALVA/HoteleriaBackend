package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.TurnoResponse;
import com.hoteleria.quantum.entity.Turno;
import com.hoteleria.quantum.entity.Usuario;
import com.hoteleria.quantum.entity.enums.EstadoTurno;
import com.hoteleria.quantum.mapper.TurnoMapper;
import com.hoteleria.quantum.repository.TurnoRepository;
import com.hoteleria.quantum.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final TurnoMapper turnoMapper;
    private final AuditService auditService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public TurnoResponse abrirTurno(Long usuarioId) {
        // Validate no existing ACTIVO turno for this user
        turnoRepository.findActivoByUsuarioId(usuarioId).ifPresent(t -> {
            throw new IllegalStateException(
                    "El usuario ya tiene un turno activo (id=" + t.getId() + ")");
        });

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + usuarioId));

        Turno turno = Turno.builder()
                .usuario(usuario)
                .fecha(LocalDate.now())
                .horaInicio(LocalTime.now())
                .estado(EstadoTurno.ACTIVO)
                .build();
        turno = turnoRepository.save(turno);

        auditService.registrarConDetalles(usuarioId, "ABRIR_TURNO", "Turno", turno.getId(),
                Map.of("fecha", turno.getFecha().toString(),
                        "horaInicio", turno.getHoraInicio().toString()));

        log.info("Turno abierto: id={} usuario={}", turno.getId(), usuario.getNombre());
        return turnoMapper.toResponse(turno);
    }

    @Transactional
    public TurnoResponse cerrarTurno(Long turnoId, Long usuarioId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Turno no encontrado con id: " + turnoId));

        if (turno.getEstado() != EstadoTurno.ACTIVO) {
            throw new IllegalStateException("El turno no está activo, estado actual: " + turno.getEstado());
        }

        turno.setHoraFin(LocalTime.now());
        turno.setEstado(EstadoTurno.CERRADO);
        turno = turnoRepository.save(turno);

        auditService.registrarConDetalles(usuarioId, "CERRAR_TURNO", "Turno", turno.getId(),
                Map.of("horaFin", turno.getHoraFin().toString()));

        log.info("Turno cerrado: id={}", turno.getId());
        return turnoMapper.toResponse(turno);
    }

    @Transactional(readOnly = true)
    public Optional<TurnoResponse> getTurnoActivo(Long usuarioId) {
        return turnoRepository.findActivoByUsuarioId(usuarioId)
                .map(turnoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TurnoResponse> findByFecha(LocalDate fecha) {
        List<Turno> turnos = turnoRepository.findByFecha(fecha);
        // Force eager load of usuario
        turnos.forEach(t -> {
            if (t.getUsuario() != null) {
                t.getUsuario().getNombre();
            }
        });
        return turnoMapper.toResponseList(turnos);
    }

    @Transactional(readOnly = true)
    public TurnoResponse findById(Long id) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Turno no encontrado con id: " + id));
        // Force eager load
        if (turno.getUsuario() != null) {
            turno.getUsuario().getNombre();
        }
        return turnoMapper.toResponse(turno);
    }
}
