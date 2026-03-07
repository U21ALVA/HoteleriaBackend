package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.CajaMovimiento;
import com.hoteleria.quantum.entity.enums.TipoMovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CajaMovimientoRepository extends JpaRepository<CajaMovimiento, Long> {

    List<CajaMovimiento> findByTurnoId(Long turnoId);

    List<CajaMovimiento> findByFechaMovimientoBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(cm.monto), 0) FROM CajaMovimiento cm " +
           "WHERE cm.tipo = :tipo AND cm.turno.id = :turnoId")
    BigDecimal sumMontoByTipoAndTurnoId(@Param("tipo") TipoMovimientoCaja tipo,
                                        @Param("turnoId") Long turnoId);

    @Query("SELECT COALESCE(SUM(cm.monto), 0) FROM CajaMovimiento cm " +
           "WHERE cm.tipo = :tipo AND cm.fechaMovimiento BETWEEN :inicio AND :fin")
    BigDecimal sumMontoByTipoAndFechaMovimientoBetween(@Param("tipo") TipoMovimientoCaja tipo,
                                                        @Param("inicio") LocalDateTime inicio,
                                                        @Param("fin") LocalDateTime fin);
}
