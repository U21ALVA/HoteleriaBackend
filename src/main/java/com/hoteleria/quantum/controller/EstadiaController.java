package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.dto.ApiResponse;
import com.hoteleria.quantum.dto.CheckinRequest;
import com.hoteleria.quantum.dto.CheckoutRequest;
import com.hoteleria.quantum.dto.EstadiaCreateRequest;
import com.hoteleria.quantum.dto.EstadiaResponse;
import com.hoteleria.quantum.entity.enums.EstadoEstadia;
import com.hoteleria.quantum.security.UserDetailsImpl;
import com.hoteleria.quantum.service.EstadiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/estadias")
@RequiredArgsConstructor
public class EstadiaController {

    private final EstadiaService estadiaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EstadiaResponse>>> findAll(
            @RequestParam(required = false) String estado) {
        EstadoEstadia estadoEnum = null;
        if (estado != null && !estado.isBlank()) {
            estadoEnum = EstadoEstadia.valueOf(estado.toUpperCase());
        }
        List<EstadiaResponse> estadias = estadiaService.findAll(estadoEnum);
        return ResponseEntity.ok(ApiResponse.ok("Estadías obtenidas", estadias));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EstadiaResponse>> findById(@PathVariable Long id) {
        EstadiaResponse estadia = estadiaService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok("Estadía encontrada", estadia));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ApiResponse<EstadiaResponse>> findByCodigo(@PathVariable String codigo) {
        EstadiaResponse estadia = estadiaService.findByCodigo(codigo);
        return ResponseEntity.ok(ApiResponse.ok("Estadía encontrada", estadia));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EstadiaResponse>> registrar(
            @Valid @RequestBody EstadiaCreateRequest request) {
        Long userId = getCurrentUserId();
        EstadiaResponse response = estadiaService.registrar(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Estadía registrada", response));
    }

    @PostMapping("/{id}/checkin")
    public ResponseEntity<ApiResponse<EstadiaResponse>> checkin(
            @PathVariable Long id,
            @Valid @RequestBody CheckinRequest request) {
        Long userId = getCurrentUserId();
        EstadiaResponse response = estadiaService.checkin(id, request, userId);
        return ResponseEntity.ok(ApiResponse.ok("Check-in realizado", response));
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<ApiResponse<EstadiaResponse>> checkout(
            @PathVariable Long id,
            @Valid @RequestBody CheckoutRequest request) {
        Long userId = getCurrentUserId();
        EstadiaResponse response = estadiaService.checkout(id, request, userId);
        return ResponseEntity.ok(ApiResponse.ok("Check-out realizado", response));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<EstadiaResponse>> cancelar(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        EstadiaResponse response = estadiaService.cancelar(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Estadía cancelada", response));
    }

    @PutMapping("/{id}/extender")
    public ResponseEntity<ApiResponse<EstadiaResponse>> extender(
            @PathVariable Long id,
            @RequestParam LocalDateTime nuevaFechaCheckout,
            @RequestParam Integer nochesExtra) {
        Long userId = getCurrentUserId();
        EstadiaResponse response = estadiaService.extender(id, nuevaFechaCheckout, nochesExtra, userId);
        return ResponseEntity.ok(ApiResponse.ok("Estadía extendida", response));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return userDetails.getId();
    }
}
