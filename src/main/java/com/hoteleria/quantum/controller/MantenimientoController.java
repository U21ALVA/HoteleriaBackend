package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.dto.ApiResponse;
import com.hoteleria.quantum.dto.MantenimientoRequest;
import com.hoteleria.quantum.dto.MantenimientoResolverRequest;
import com.hoteleria.quantum.dto.MantenimientoResponse;
import com.hoteleria.quantum.security.UserDetailsImpl;
import com.hoteleria.quantum.service.MantenimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mantenimientos")
@RequiredArgsConstructor
public class MantenimientoController {

    private final MantenimientoService mantenimientoService;

    @PostMapping
    public ResponseEntity<ApiResponse<MantenimientoResponse>> reportar(
            @Valid @RequestBody MantenimientoRequest request) {
        Long userId = getCurrentUserId();
        MantenimientoResponse response = mantenimientoService.reportar(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Mantenimiento reportado", response));
    }

    @PatchMapping("/{id}/resolver")
    public ResponseEntity<ApiResponse<MantenimientoResponse>> resolver(
            @PathVariable Long id,
            @RequestBody MantenimientoResolverRequest request) {
        Long userId = getCurrentUserId();
        MantenimientoResponse response = mantenimientoService.resolver(id, request, userId);
        return ResponseEntity.ok(ApiResponse.ok("Mantenimiento resuelto", response));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<ApiResponse<List<MantenimientoResponse>>> findPendientes() {
        List<MantenimientoResponse> pendientes = mantenimientoService.findPendientes();
        return ResponseEntity.ok(ApiResponse.ok("Mantenimientos pendientes obtenidos", pendientes));
    }

    @GetMapping("/habitacion/{habitacionId}")
    public ResponseEntity<ApiResponse<List<MantenimientoResponse>>> findByHabitacionId(
            @PathVariable Integer habitacionId) {
        List<MantenimientoResponse> mantenimientos = mantenimientoService.findByHabitacionId(habitacionId);
        return ResponseEntity.ok(ApiResponse.ok("Mantenimientos de habitación obtenidos", mantenimientos));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MantenimientoResponse>>> findAll() {
        List<MantenimientoResponse> mantenimientos = mantenimientoService.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Mantenimientos obtenidos", mantenimientos));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return userDetails.getId();
    }
}
