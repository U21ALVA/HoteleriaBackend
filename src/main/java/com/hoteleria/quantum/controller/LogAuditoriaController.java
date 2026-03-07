package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.dto.ApiResponse;
import com.hoteleria.quantum.dto.LogAuditoriaResponse;
import com.hoteleria.quantum.dto.PageResponse;
import com.hoteleria.quantum.service.LogAuditoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/auditoria")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class LogAuditoriaController {

    private final LogAuditoriaService logAuditoriaService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LogAuditoriaResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LogAuditoriaResponse> resultPage = logAuditoriaService.findAll(pageable);
        PageResponse<LogAuditoriaResponse> pageResponse = PageResponse.from(resultPage);
        return ResponseEntity.ok(ApiResponse.ok("Logs de auditoría obtenidos", pageResponse));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<List<LogAuditoriaResponse>>> findByUsuarioId(
            @PathVariable Long usuarioId) {
        List<LogAuditoriaResponse> logs = logAuditoriaService.findByUsuarioId(usuarioId);
        return ResponseEntity.ok(ApiResponse.ok("Logs de auditoría por usuario obtenidos", logs));
    }

    @GetMapping("/entidad/{entidad}")
    public ResponseEntity<ApiResponse<List<LogAuditoriaResponse>>> findByEntidad(
            @PathVariable String entidad) {
        List<LogAuditoriaResponse> logs = logAuditoriaService.findByEntidad(entidad);
        return ResponseEntity.ok(ApiResponse.ok("Logs de auditoría por entidad obtenidos", logs));
    }

    @GetMapping("/rango")
    public ResponseEntity<ApiResponse<List<LogAuditoriaResponse>>> findByRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);
        List<LogAuditoriaResponse> logs = logAuditoriaService.findByRango(inicio, fin);
        return ResponseEntity.ok(ApiResponse.ok("Logs de auditoría por rango obtenidos", logs));
    }
}
