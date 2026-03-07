package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.DashboardResponse;
import com.hoteleria.quantum.entity.enums.EstadoEstadia;
import com.hoteleria.quantum.entity.enums.EstadoHabitacion;
import com.hoteleria.quantum.entity.enums.MetodoPago;
import com.hoteleria.quantum.entity.enums.TipoMovimientoCaja;
import com.hoteleria.quantum.repository.CajaMovimientoRepository;
import com.hoteleria.quantum.repository.EstadiaRepository;
import com.hoteleria.quantum.repository.HabitacionRepository;
import com.hoteleria.quantum.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CajaMovimientoRepository cajaMovimientoRepository;
    private final EstadiaRepository estadiaRepository;
    private final HabitacionRepository habitacionRepository;
    private final PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);

        // Ingresos del día (COALESCE in query ensures non-null)
        BigDecimal ingresosDia = Optional.ofNullable(
                cajaMovimientoRepository.sumMontoByTipoAndFechaMovimientoBetween(
                        TipoMovimientoCaja.INGRESO, startOfDay, endOfDay))
                .orElse(BigDecimal.ZERO);

        // Ingresos del mes
        BigDecimal ingresosMes = Optional.ofNullable(
                cajaMovimientoRepository.sumMontoByTipoAndFechaMovimientoBetween(
                        TipoMovimientoCaja.INGRESO, startOfMonth, endOfMonth))
                .orElse(BigDecimal.ZERO);

        // Estadías activas
        int estadiasActivas = estadiaRepository.findByEstado(EstadoEstadia.ACTIVA).size();

        // Habitaciones ocupadas y disponibles
        long habitacionesOcupadas = Optional.ofNullable(
                habitacionRepository.countByEstado(EstadoHabitacion.OCUPADA)).orElse(0L);
        long habitacionesDisponibles = Optional.ofNullable(
                habitacionRepository.countByEstado(EstadoHabitacion.DISPONIBLE)).orElse(0L);

        // Checkouts hoy (COMPLETADA today)
        long checkoutsHoy = Optional.ofNullable(
                estadiaRepository.countByEstadoAndFechaRegistroBetween(
                        EstadoEstadia.COMPLETADA, startOfDay, endOfDay)).orElse(0L);

        // Ocupación porcentaje
        long totalActivas = habitacionRepository.findByActivoTrue().size();
        BigDecimal ocupacionPorcentaje = BigDecimal.ZERO;
        if (totalActivas > 0) {
            ocupacionPorcentaje = BigDecimal.valueOf(habitacionesOcupadas)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalActivas), 2, RoundingMode.HALF_UP);
        }

        // Ingresos por método de pago (current month)
        Map<String, BigDecimal> ingresosPorMetodo = new HashMap<>();
        for (MetodoPago metodo : MetodoPago.values()) {
            BigDecimal monto = Optional.ofNullable(
                    pagoRepository.sumMontoByMetodoPagoAndFechaPagoBetween(
                            metodo, startOfMonth, endOfMonth))
                    .orElse(BigDecimal.ZERO);
            ingresosPorMetodo.put(metodo.name(), monto);
        }

        return DashboardResponse.builder()
                .ingresosDia(ingresosDia)
                .ingresosMes(ingresosMes)
                .estadiasActivas(estadiasActivas)
                .habitacionesOcupadas((int) habitacionesOcupadas)
                .habitacionesDisponibles((int) habitacionesDisponibles)
                .checkoutsHoy((int) checkoutsHoy)
                .ocupacionPorcentaje(ocupacionPorcentaje)
                .ingresosPorMetodo(ingresosPorMetodo)
                .build();
    }
}
