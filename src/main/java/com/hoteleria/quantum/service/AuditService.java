package com.hoteleria.quantum.service;

import com.hoteleria.quantum.entity.LogAuditoria;
import com.hoteleria.quantum.entity.Usuario;
import com.hoteleria.quantum.repository.LogAuditoriaRepository;
import com.hoteleria.quantum.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final LogAuditoriaRepository logAuditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Async
    @Transactional
    public void registrar(Long usuarioId, String accion, String entidadAfectada,
                          Long entidadId, Map<String, Object> detalles, String ipAddress) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
            LogAuditoria logAuditoria = LogAuditoria.builder()
                    .usuario(usuario)
                    .accion(accion)
                    .entidadAfectada(entidadAfectada)
                    .entidadId(entidadId)
                    .detalles(detalles != null ? detalles : Map.of())
                    .ipAddress(ipAddress)
                    .build();
            logAuditoriaRepository.save(logAuditoria);
        } catch (Exception e) {
            log.error("Error registrando auditoría: {}", e.getMessage());
        }
    }

    public void registrar(Long usuarioId, String accion, String entidadAfectada, Long entidadId) {
        registrar(usuarioId, accion, entidadAfectada, entidadId, null, null);
    }

    public void registrarConDetalles(Long usuarioId, String accion, String entidadAfectada,
                                     Long entidadId, Map<String, Object> detalles) {
        registrar(usuarioId, accion, entidadAfectada, entidadId, detalles, null);
    }
}
