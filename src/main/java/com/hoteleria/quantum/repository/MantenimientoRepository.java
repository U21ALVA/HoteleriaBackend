package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {

    List<Mantenimiento> findByHabitacionId(Integer habitacionId);

    List<Mantenimiento> findByResueltoFalse();

    List<Mantenimiento> findByResueltoFalseOrderByFechaReporteDesc();

    List<Mantenimiento> findByHabitacionIdAndResueltoFalse(Integer habitacionId);
}
