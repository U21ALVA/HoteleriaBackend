package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.dto.ApiResponse;
import com.hoteleria.quantum.dto.PagoRequest;
import com.hoteleria.quantum.dto.PagoResponse;
import com.hoteleria.quantum.security.UserDetailsImpl;
import com.hoteleria.quantum.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping("/estadia/{estadiaId}")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> findByEstadiaId(
            @PathVariable Long estadiaId) {
        List<PagoResponse> pagos = pagoService.findByEstadiaId(estadiaId);
        return ResponseEntity.ok(ApiResponse.ok("Pagos de estadía obtenidos", pagos));
    }

    @GetMapping("/turno/{turnoId}")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> findByTurnoId(
            @PathVariable Long turnoId) {
        List<PagoResponse> pagos = pagoService.findByTurnoId(turnoId);
        return ResponseEntity.ok(ApiResponse.ok("Pagos de turno obtenidos", pagos));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PagoResponse>> registrar(
            @Valid @RequestBody PagoRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();

        PagoResponse response = pagoService.registrar(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pago registrado", response));
    }

    @GetMapping("/estadia/{estadiaId}/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getSumByEstadia(
            @PathVariable Long estadiaId) {
        BigDecimal total = pagoService.getSumByEstadia(estadiaId);
        return ResponseEntity.ok(ApiResponse.ok("Total de pagos", total));
    }
}
