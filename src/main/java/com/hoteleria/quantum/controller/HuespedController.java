package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.dto.ApiResponse;
import com.hoteleria.quantum.dto.HuespedRequest;
import com.hoteleria.quantum.dto.HuespedResponse;
import com.hoteleria.quantum.service.HuespedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/huespedes")
@RequiredArgsConstructor
public class HuespedController {

    private final HuespedService huespedService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HuespedResponse>>> findAll() {
        List<HuespedResponse> huespedes = huespedService.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Huéspedes obtenidos", huespedes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HuespedResponse>> findById(@PathVariable Long id) {
        HuespedResponse huesped = huespedService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok("Huésped encontrado", huesped));
    }

    @GetMapping("/documento/{documento}")
    public ResponseEntity<ApiResponse<HuespedResponse>> findByDocumento(@PathVariable String documento) {
        HuespedResponse huesped = huespedService.findByDocumento(documento);
        return ResponseEntity.ok(ApiResponse.ok("Huésped encontrado", huesped));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<HuespedResponse>>> search(@RequestParam String q) {
        List<HuespedResponse> huespedes = huespedService.search(q);
        return ResponseEntity.ok(ApiResponse.ok("Resultados de búsqueda", huespedes));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HuespedResponse>> create(
            @Valid @RequestBody HuespedRequest request) {
        HuespedResponse response = huespedService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Huésped creado", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HuespedResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody HuespedRequest request) {
        HuespedResponse response = huespedService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Huésped actualizado", response));
    }
}
