package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.Turno;
import com.hoteleria.quantum.entity.enums.EstadoTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByUsuarioIdAndEstado(Long usuarioId, EstadoTurno estado);

    List<Turno> findByEstado(EstadoTurno estado);

    List<Turno> findByFecha(LocalDate fecha);

    @Query("SELECT t FROM Turno t WHERE t.usuario.id = :usuarioId " +
           "AND t.estado = com.hoteleria.quantum.entity.enums.EstadoTurno.ACTIVO")
    Optional<Turno> findActivoByUsuarioId(@Param("usuarioId") Long usuarioId);
}
