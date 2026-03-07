package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.CardexCierre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardexCierreRepository extends JpaRepository<CardexCierre, Long> {

    Optional<CardexCierre> findByTurnoId(Long turnoId);

    List<CardexCierre> findByFecha(LocalDate fecha);

    List<CardexCierre> findByFechaBetween(LocalDate inicio, LocalDate fin);
}
