package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.dto.ApiResponse;
import com.hoteleria.quantum.dto.TurnoResponse;
import com.hoteleria.quantum.security.UserDetailsImpl;
import com.hoteleria.quantum.service.TurnoService;
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
@RequestMapping("/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @PostMapping("/abrir")
    public ResponseEntity<ApiResponse<TurnoResponse>> abrirTurno() {
        Long userId = getCurrentUserId();
        TurnoResponse response = turnoService.abrirTurno(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Turno abierto exitosamente", response));
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<ApiResponse<TurnoResponse>> cerrarTurno(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        TurnoResponse response = turnoService.cerrarTurno(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Turno cerrado exitosamente", response));
    }

    @GetMapping("/activo")
    public ResponseEntity<ApiResponse<TurnoResponse>> getTurnoActivo() {
        Long userId = getCurrentUserId();
        return turnoService.getTurnoActivo(userId)
                .map(turno -> ResponseEntity.ok(ApiResponse.ok("Turno activo encontrado", turno)))
                .orElse(ResponseEntity.ok(ApiResponse.ok("Sin turno activo", null)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TurnoResponse>>> findByFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate fechaBusqueda = fecha != null ? fecha : LocalDate.now();
        List<TurnoResponse> turnos = turnoService.findByFecha(fechaBusqueda);
        return ResponseEntity.ok(ApiResponse.ok("Turnos obtenidos", turnos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TurnoResponse>> findById(@PathVariable Long id) {
        TurnoResponse response = turnoService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok("Turno encontrado", response));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return userDetails.getId();
    }
}
