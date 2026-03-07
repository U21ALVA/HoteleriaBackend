package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.dto.ApiResponse;
import com.hoteleria.quantum.dto.TarifaTemporadaRequest;
import com.hoteleria.quantum.dto.TarifaTemporadaResponse;
import com.hoteleria.quantum.service.TarifaTemporadaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/tarifas")
@RequiredArgsConstructor
public class TarifaTemporadaController {

    private final TarifaTemporadaService tarifaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TarifaTemporadaResponse>>> findAll() {
        List<TarifaTemporadaResponse> tarifas = tarifaService.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Tarifas obtenidas", tarifas));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<ApiResponse<List<TarifaTemporadaResponse>>> findByCategoriaId(
            @PathVariable Integer categoriaId) {
        List<TarifaTemporadaResponse> tarifas = tarifaService.findByCategoriaId(categoriaId);
        return ResponseEntity.ok(ApiResponse.ok("Tarifas por categoría obtenidas", tarifas));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<TarifaTemporadaResponse>> create(
            @Valid @RequestBody TarifaTemporadaRequest request) {
        TarifaTemporadaResponse response = tarifaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tarifa creada", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<TarifaTemporadaResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody TarifaTemporadaRequest request) {
        TarifaTemporadaResponse response = tarifaService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Tarifa actualizada", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        tarifaService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
