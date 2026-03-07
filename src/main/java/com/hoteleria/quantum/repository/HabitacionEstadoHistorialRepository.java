package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.HabitacionEstadoHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HabitacionEstadoHistorialRepository extends JpaRepository<HabitacionEstadoHistorial, Long> {

    List<HabitacionEstadoHistorial> findByHabitacionIdOrderByFechaDesc(Integer habitacionId);

    List<HabitacionEstadoHistorial> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
}
