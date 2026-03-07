package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.TarifaTemporada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TarifaTemporadaRepository extends JpaRepository<TarifaTemporada, Integer> {

    List<TarifaTemporada> findByActivoTrue();

    List<TarifaTemporada> findByCategoriaId(Integer categoriaId);

    @Query("SELECT t FROM TarifaTemporada t WHERE t.categoria.id = :categoriaId " +
           "AND t.activo = true AND :fecha BETWEEN t.fechaInicio AND t.fechaFin")
    List<TarifaTemporada> findActiveByCategoriaIdAndFecha(@Param("categoriaId") Integer categoriaId,
                                                          @Param("fecha") LocalDate fecha);
}
