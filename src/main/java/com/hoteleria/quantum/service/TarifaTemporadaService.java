package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.TarifaTemporadaRequest;
import com.hoteleria.quantum.dto.TarifaTemporadaResponse;
import com.hoteleria.quantum.entity.CategoriaHabitacion;
import com.hoteleria.quantum.entity.TarifaTemporada;
import com.hoteleria.quantum.repository.CategoriaHabitacionRepository;
import com.hoteleria.quantum.repository.TarifaTemporadaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TarifaTemporadaService {

    private final TarifaTemporadaRepository tarifaRepository;
    private final CategoriaHabitacionRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<TarifaTemporadaResponse> findAll() {
        List<TarifaTemporada> tarifas = tarifaRepository.findByActivoTrue();
        // Force eager load of categoria within transaction
        tarifas.forEach(t -> {
            if (t.getCategoria() != null) {
                t.getCategoria().getNombre();
            }
        });
        return tarifas.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TarifaTemporadaResponse> findByCategoriaId(Integer categoriaId) {
        categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Categoría no encontrada con id: " + categoriaId));

        List<TarifaTemporada> tarifas = tarifaRepository.findByCategoriaId(categoriaId);
        tarifas.forEach(t -> {
            if (t.getCategoria() != null) {
                t.getCategoria().getNombre();
            }
        });
        return tarifas.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TarifaTemporadaResponse create(TarifaTemporadaRequest request) {
        CategoriaHabitacion categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Categoría no encontrada con id: " + request.getCategoriaId()));

        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de fin debe ser posterior a la fecha de inicio");
        }

        TarifaTemporada tarifa = TarifaTemporada.builder()
                .categoria(categoria)
                .nombre(request.getNombre())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .precioModificado(request.getPrecioModificado())
                .activo(true)
                .build();
        tarifa = tarifaRepository.save(tarifa);

        log.info("Tarifa de temporada creada: {} (id={})", tarifa.getNombre(), tarifa.getId());
        return toResponse(tarifa);
    }

    @Transactional
    public TarifaTemporadaResponse update(Integer id, TarifaTemporadaRequest request) {
        TarifaTemporada tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tarifa de temporada no encontrada con id: " + id));

        CategoriaHabitacion categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Categoría no encontrada con id: " + request.getCategoriaId()));

        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de fin debe ser posterior a la fecha de inicio");
        }

        tarifa.setCategoria(categoria);
        tarifa.setNombre(request.getNombre());
        tarifa.setFechaInicio(request.getFechaInicio());
        tarifa.setFechaFin(request.getFechaFin());
        tarifa.setPrecioModificado(request.getPrecioModificado());
        tarifa = tarifaRepository.save(tarifa);

        log.info("Tarifa de temporada actualizada: {} (id={})", tarifa.getNombre(), tarifa.getId());
        return toResponse(tarifa);
    }

    @Transactional
    public void deactivate(Integer id) {
        TarifaTemporada tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tarifa de temporada no encontrada con id: " + id));

        tarifa.setActivo(false);
        tarifaRepository.save(tarifa);
        log.info("Tarifa de temporada desactivada: {} (id={})", tarifa.getNombre(), tarifa.getId());
    }

    private TarifaTemporadaResponse toResponse(TarifaTemporada tarifa) {
        return TarifaTemporadaResponse.builder()
                .id(tarifa.getId().longValue())
                .categoriaId(tarifa.getCategoria() != null ? tarifa.getCategoria().getId() : null)
                .categoriaNombre(tarifa.getCategoria() != null ? tarifa.getCategoria().getNombre() : null)
                .nombre(tarifa.getNombre())
                .fechaInicio(tarifa.getFechaInicio())
                .fechaFin(tarifa.getFechaFin())
                .precioModificado(tarifa.getPrecioModificado())
                .activo(tarifa.getActivo())
                .creadoEn(tarifa.getCreadoEn())
                .build();
    }
}
