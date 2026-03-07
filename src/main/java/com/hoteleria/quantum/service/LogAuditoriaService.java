package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.LogAuditoriaResponse;
import com.hoteleria.quantum.entity.LogAuditoria;
import com.hoteleria.quantum.mapper.LogAuditoriaMapper;
import com.hoteleria.quantum.repository.LogAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogAuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;
    private final LogAuditoriaMapper logAuditoriaMapper;

    @Transactional(readOnly = true)
    public Page<LogAuditoriaResponse> findAll(Pageable pageable) {
        Page<LogAuditoria> page = logAuditoriaRepository.findAllByOrderByFechaHoraDesc(pageable);
        // Force eager load within transaction
        page.getContent().forEach(this::forceEagerLoad);
        return page.map(logAuditoriaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<LogAuditoriaResponse> findByUsuarioId(Long usuarioId) {
        List<LogAuditoria> logs = logAuditoriaRepository.findByUsuarioId(usuarioId);
        logs.forEach(this::forceEagerLoad);
        return logAuditoriaMapper.toResponseList(logs);
    }

    @Transactional(readOnly = true)
    public List<LogAuditoriaResponse> findByEntidad(String entidad) {
        List<LogAuditoria> logs = logAuditoriaRepository.findByEntidadAfectada(entidad);
        logs.forEach(this::forceEagerLoad);
        return logAuditoriaMapper.toResponseList(logs);
    }

    @Transactional(readOnly = true)
    public List<LogAuditoriaResponse> findByRango(LocalDateTime inicio, LocalDateTime fin) {
        List<LogAuditoria> logs = logAuditoriaRepository.findByFechaHoraBetween(inicio, fin);
        logs.forEach(this::forceEagerLoad);
        return logAuditoriaMapper.toResponseList(logs);
    }

    private void forceEagerLoad(LogAuditoria logAuditoria) {
        if (logAuditoria.getUsuario() != null) {
            logAuditoria.getUsuario().getNombre();
        }
    }
}
