package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.MantenimientoProgramado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MantenimientoProgramadoRepository extends JpaRepository<MantenimientoProgramado, Integer> {

    List<MantenimientoProgramado> findByActivoTrue();

    List<MantenimientoProgramado> findByProximaEjecucionLessThanEqual(LocalDate fecha);
}
