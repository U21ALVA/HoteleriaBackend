package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.Pago;
import com.hoteleria.quantum.entity.enums.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByEstadiaId(Long estadiaId);

    List<Pago> findByTurnoId(Long turnoId);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.estadia.id = :estadiaId")
    BigDecimal sumMontoByEstadiaId(@Param("estadiaId") Long estadiaId);

    List<Pago> findByFechaPagoBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.metodoPago = :metodoPago " +
           "AND p.fechaPago BETWEEN :inicio AND :fin")
    BigDecimal sumMontoByMetodoPagoAndFechaPagoBetween(@Param("metodoPago") MetodoPago metodoPago,
                                                       @Param("inicio") LocalDateTime inicio,
                                                       @Param("fin") LocalDateTime fin);
}
