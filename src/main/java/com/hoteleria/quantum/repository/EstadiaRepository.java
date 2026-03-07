package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.Estadia;
import com.hoteleria.quantum.entity.enums.EstadoEstadia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstadiaRepository extends JpaRepository<Estadia, Long> {

    Optional<Estadia> findByCodigo(String codigo);

    List<Estadia> findByEstado(EstadoEstadia estado);

    List<Estadia> findByHuespedId(Long huespedId);

    List<Estadia> findByEstadoIn(List<EstadoEstadia> estados);

    @Query("SELECT e FROM Estadia e JOIN EstadiaHabitacion eh ON eh.estadia = e " +
           "WHERE eh.habitacion.id = :habitacionId AND e.estado IN (com.hoteleria.quantum.entity.enums.EstadoEstadia.ACTIVA, " +
           "com.hoteleria.quantum.entity.enums.EstadoEstadia.REGISTRADA)")
    List<Estadia> findActiveByHabitacionId(@Param("habitacionId") Integer habitacionId);

    @Query("SELECT e FROM Estadia e WHERE e.fechaRegistro BETWEEN :inicio AND :fin")
    List<Estadia> findByFechaRegistroBetween(@Param("inicio") LocalDateTime inicio,
                                             @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(e) FROM Estadia e WHERE e.estado = :estado " +
           "AND e.fechaRegistro BETWEEN :inicio AND :fin")
    Long countByEstadoAndFechaRegistroBetween(@Param("estado") EstadoEstadia estado,
                                              @Param("inicio") LocalDateTime inicio,
                                              @Param("fin") LocalDateTime fin);
}
