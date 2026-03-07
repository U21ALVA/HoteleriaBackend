package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.Habitacion;
import com.hoteleria.quantum.entity.enums.EstadoHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Integer> {

    Optional<Habitacion> findByNumero(String numero);

    List<Habitacion> findByEstado(EstadoHabitacion estado);

    List<Habitacion> findByActivoTrue();

    List<Habitacion> findByPiso(Integer piso);

    List<Habitacion> findByCategoriaId(Integer categoriaId);

    List<Habitacion> findByEstadoAndActivoTrue(EstadoHabitacion estado);

    Boolean existsByNumero(String numero);

    Long countByEstado(EstadoHabitacion estado);
}
