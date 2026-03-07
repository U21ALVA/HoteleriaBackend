package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.CategoriaHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaHabitacionRepository extends JpaRepository<CategoriaHabitacion, Integer> {

    List<CategoriaHabitacion> findByActivoTrue();

    Optional<CategoriaHabitacion> findByNombre(String nombre);

    Boolean existsByNombre(String nombre);
}
