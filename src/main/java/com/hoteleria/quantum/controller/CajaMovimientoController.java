package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.dto.ApiResponse;
import com.hoteleria.quantum.dto.CajaMovimientoRequest;
import com.hoteleria.quantum.dto.CajaMovimientoResponse;
import com.hoteleria.quantum.security.UserDetailsImpl;
import com.hoteleria.quantum.service.CajaMovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/caja")
@RequiredArgsConstructor
public class CajaMovimientoController {

    private final CajaMovimientoService cajaMovimientoService;

    @PostMapping
    public ResponseEntity<ApiResponse<CajaMovimientoResponse>> registrarMovimiento(
            @Valid @RequestBody CajaMovimientoRequest request) {
        Long userId = getCurrentUserId();
        CajaMovimientoResponse response = cajaMovimientoService.registrarMovimiento(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Movimiento de caja registrado", response));
    }

    @GetMapping("/turno/{turnoId}")
    public ResponseEntity<ApiResponse<List<CajaMovimientoResponse>>> findByTurnoId(
            @PathVariable Long turnoId) {
        List<CajaMovimientoResponse> movimientos = cajaMovimientoService.findByTurnoId(turnoId);
        return ResponseEntity.ok(ApiResponse.ok("Movimientos del turno obtenidos", movimientos));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CajaMovimientoResponse>>> findByFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);
        List<CajaMovimientoResponse> movimientos = cajaMovimientoService.findByFecha(inicio, fin);
        return ResponseEntity.ok(ApiResponse.ok("Movimientos obtenidos", movimientos));
    }

    @GetMapping("/turno/{turnoId}/totales")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTotalesPorTurno(
            @PathVariable Long turnoId) {
        Map<String, Object> totales = cajaMovimientoService.getTotalesPorTurno(turnoId);
        return ResponseEntity.ok(ApiResponse.ok("Totales del turno obtenidos", totales));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return userDetails.getId();
    }
}
