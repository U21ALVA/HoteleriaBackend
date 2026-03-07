package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.EstadiaHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstadiaHabitacionRepository extends JpaRepository<EstadiaHabitacion, Long> {

    List<EstadiaHabitacion> findByEstadiaId(Long estadiaId);

    List<EstadiaHabitacion> findByHabitacionId(Integer habitacionId);

    @Query("SELECT eh FROM EstadiaHabitacion eh WHERE eh.habitacion.id = :habitacionId " +
           "AND eh.estadia.estado IN (com.hoteleria.quantum.entity.enums.EstadoEstadia.ACTIVA, " +
           "com.hoteleria.quantum.entity.enums.EstadoEstadia.REGISTRADA)")
    List<EstadiaHabitacion> findActiveByHabitacionId(@Param("habitacionId") Integer habitacionId);
}
