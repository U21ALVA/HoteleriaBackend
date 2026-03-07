package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.CategoriaHabitacionRequest;
import com.hoteleria.quantum.dto.CategoriaHabitacionResponse;
import com.hoteleria.quantum.entity.CategoriaHabitacion;
import com.hoteleria.quantum.mapper.CategoriaHabitacionMapper;
import com.hoteleria.quantum.repository.CategoriaHabitacionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaHabitacionService {

    private final CategoriaHabitacionRepository categoriaRepository;
    private final CategoriaHabitacionMapper categoriaMapper;

    @Transactional(readOnly = true)
    public List<CategoriaHabitacionResponse> findAll() {
        List<CategoriaHabitacion> categorias = categoriaRepository.findByActivoTrue();
        return categoriaMapper.toResponseList(categorias);
    }

    @Transactional(readOnly = true)
    public CategoriaHabitacionResponse findById(Integer id) {
        CategoriaHabitacion categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Categoría no encontrada con id: " + id));
        return categoriaMapper.toResponse(categoria);
    }

    @Transactional
    public CategoriaHabitacionResponse create(CategoriaHabitacionRequest request) {
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new IllegalArgumentException(
                    "Ya existe una categoría con el nombre: " + request.getNombre());
        }

        CategoriaHabitacion categoria = categoriaMapper.toEntity(request);
        categoria.setActivo(true);
        categoria = categoriaRepository.save(categoria);

        log.info("Categoría creada: {} (id={})", categoria.getNombre(), categoria.getId());
        return categoriaMapper.toResponse(categoria);
    }

    @Transactional
    public CategoriaHabitacionResponse update(Integer id, CategoriaHabitacionRequest request) {
        CategoriaHabitacion categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Categoría no encontrada con id: " + id));

        // Check uniqueness only if name changed
        if (!categoria.getNombre().equals(request.getNombre())
                && categoriaRepository.existsByNombre(request.getNombre())) {
            throw new IllegalArgumentException(
                    "Ya existe una categoría con el nombre: " + request.getNombre());
        }

        categoria.setNombre(request.getNombre());
        categoria.setPrecioBase(request.getPrecioBase());
        categoria.setCapacidad(request.getCapacidad());
        categoria.setCaracteristicas(request.getCaracteristicas());
        categoria = categoriaRepository.save(categoria);

        log.info("Categoría actualizada: {} (id={})", categoria.getNombre(), categoria.getId());
        return categoriaMapper.toResponse(categoria);
    }

    @Transactional
    public void deactivate(Integer id) {
        CategoriaHabitacion categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Categoría no encontrada con id: " + id));

        categoria.setActivo(false);
        categoriaRepository.save(categoria);
        log.info("Categoría desactivada: {} (id={})", categoria.getNombre(), categoria.getId());
    }
}
