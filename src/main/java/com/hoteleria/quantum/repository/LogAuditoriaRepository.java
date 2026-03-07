package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.LogAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {

    List<LogAuditoria> findByUsuarioId(Long usuarioId);

    List<LogAuditoria> findByEntidadAfectada(String entidad);

    List<LogAuditoria> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    Page<LogAuditoria> findAllByOrderByFechaHoraDesc(Pageable pageable);
}
