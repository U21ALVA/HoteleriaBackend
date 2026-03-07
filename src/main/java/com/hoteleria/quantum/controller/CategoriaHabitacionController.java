package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.dto.ApiResponse;
import com.hoteleria.quantum.dto.CategoriaHabitacionRequest;
import com.hoteleria.quantum.dto.CategoriaHabitacionResponse;
import com.hoteleria.quantum.service.CategoriaHabitacionService;
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
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaHabitacionController {

    private final CategoriaHabitacionService categoriaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaHabitacionResponse>>> findAll() {
        List<CategoriaHabitacionResponse> categorias = categoriaService.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Categorías obtenidas", categorias));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaHabitacionResponse>> findById(@PathVariable Integer id) {
        CategoriaHabitacionResponse categoria = categoriaService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok("Categoría encontrada", categoria));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<CategoriaHabitacionResponse>> create(
            @Valid @RequestBody CategoriaHabitacionRequest request) {
        CategoriaHabitacionResponse response = categoriaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Categoría creada", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<CategoriaHabitacionResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaHabitacionRequest request) {
        CategoriaHabitacionResponse response = categoriaService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Categoría actualizada", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        categoriaService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
