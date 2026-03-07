package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.dto.ApiResponse;
import com.hoteleria.quantum.dto.CardexCierreResponse;
import com.hoteleria.quantum.security.UserDetailsImpl;
import com.hoteleria.quantum.service.CardexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cardex")
@RequiredArgsConstructor
public class CardexController {

    private final CardexService cardexService;

    @PostMapping("/turno/{turnoId}")
    public ResponseEntity<ApiResponse<CardexCierreResponse>> generarCierre(
            @PathVariable Long turnoId,
            @RequestParam(required = false) String observaciones) {
        Long userId = getCurrentUserId();
        CardexCierreResponse response = cardexService.generarCierre(turnoId, observaciones, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cierre de cárdex generado", response));
    }

    @GetMapping("/turno/{turnoId}")
    public ResponseEntity<ApiResponse<CardexCierreResponse>> findByTurnoId(
            @PathVariable Long turnoId) {
        CardexCierreResponse response = cardexService.findByTurnoId(turnoId);
        return ResponseEntity.ok(ApiResponse.ok("Cierre de cárdex encontrado", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CardexCierreResponse>>> findByFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<CardexCierreResponse> cierres = cardexService.findByFecha(fecha);
        return ResponseEntity.ok(ApiResponse.ok("Cierres de cárdex obtenidos", cierres));
    }

    @GetMapping("/rango")
    public ResponseEntity<ApiResponse<List<CardexCierreResponse>>> findByRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<CardexCierreResponse> cierres = cardexService.findByRango(desde, hasta);
        return ResponseEntity.ok(ApiResponse.ok("Cierres de cárdex por rango obtenidos", cierres));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return userDetails.getId();
    }
}
