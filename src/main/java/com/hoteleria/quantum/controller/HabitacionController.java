package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.dto.ApiResponse;
import com.hoteleria.quantum.dto.HabitacionEstadoRequest;
import com.hoteleria.quantum.dto.HabitacionRequest;
import com.hoteleria.quantum.dto.HabitacionResponse;
import com.hoteleria.quantum.entity.enums.EstadoHabitacion;
import com.hoteleria.quantum.security.UserDetailsImpl;
import com.hoteleria.quantum.service.HabitacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/habitaciones")
@RequiredArgsConstructor
public class HabitacionController {

    private final HabitacionService habitacionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HabitacionResponse>>> findAll() {
        List<HabitacionResponse> habitaciones = habitacionService.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Habitaciones obtenidas", habitaciones));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HabitacionResponse>> findById(@PathVariable Integer id) {
        HabitacionResponse habitacion = habitacionService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok("Habitación encontrada", habitacion));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<HabitacionResponse>>> findByEstado(
            @PathVariable EstadoHabitacion estado) {
        List<HabitacionResponse> habitaciones = habitacionService.findByEstado(estado);
        return ResponseEntity.ok(ApiResponse.ok("Habitaciones filtradas por estado", habitaciones));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOcupacionStats() {
        Map<String, Object> stats = habitacionService.getOcupacionStats();
        return ResponseEntity.ok(ApiResponse.ok("Estadísticas de ocupación", stats));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<HabitacionResponse>> create(
            @Valid @RequestBody HabitacionRequest request) {
        HabitacionResponse response = habitacionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Habitación creada", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<HabitacionResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody HabitacionRequest request) {
        HabitacionResponse response = habitacionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Habitación actualizada", response));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<HabitacionResponse>> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody HabitacionEstadoRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();

        HabitacionResponse response = habitacionService.cambiarEstado(id, request, userId);
        return ResponseEntity.ok(ApiResponse.ok("Estado de habitación actualizado", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        habitacionService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
