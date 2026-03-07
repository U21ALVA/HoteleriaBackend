package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.HuespedRequest;
import com.hoteleria.quantum.dto.HuespedResponse;
import com.hoteleria.quantum.entity.Huesped;
import com.hoteleria.quantum.mapper.HuespedMapper;
import com.hoteleria.quantum.repository.HuespedRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HuespedService {

    private final HuespedRepository huespedRepository;
    private final HuespedMapper huespedMapper;

    @Transactional(readOnly = true)
    public List<HuespedResponse> findAll() {
        List<Huesped> huespedes = huespedRepository.findAll();
        return huespedMapper.toResponseList(huespedes);
    }

    @Transactional(readOnly = true)
    public HuespedResponse findById(Long id) {
        Huesped huesped = huespedRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Huésped no encontrado con id: " + id));
        return huespedMapper.toResponse(huesped);
    }

    @Transactional(readOnly = true)
    public HuespedResponse findByDocumento(String documento) {
        Huesped huesped = huespedRepository.findByDocumentoIdentidad(documento)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Huésped no encontrado con documento: " + documento));
        return huespedMapper.toResponse(huesped);
    }

    @Transactional(readOnly = true)
    public List<HuespedResponse> search(String query) {
        List<Huesped> huespedes = huespedRepository.searchByNombreOrDocumento(query);
        return huespedMapper.toResponseList(huespedes);
    }

    @Transactional
    public HuespedResponse create(HuespedRequest request) {
        if (huespedRepository.existsByDocumentoIdentidad(request.getDocumentoIdentidad())) {
            throw new IllegalArgumentException(
                    "Ya existe un huésped con el documento: " + request.getDocumentoIdentidad());
        }

        Huesped huesped = huespedMapper.toEntity(request);
        huesped = huespedRepository.save(huesped);

        log.info("Huésped creado: {} (id={})", huesped.getNombreCompleto(), huesped.getId());
        return huespedMapper.toResponse(huesped);
    }

    @Transactional
    public HuespedResponse update(Long id, HuespedRequest request) {
        Huesped huesped = huespedRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Huésped no encontrado con id: " + id));

        // Check documento uniqueness if changed
        if (!huesped.getDocumentoIdentidad().equals(request.getDocumentoIdentidad())
                && huespedRepository.existsByDocumentoIdentidad(request.getDocumentoIdentidad())) {
            throw new IllegalArgumentException(
                    "Ya existe un huésped con el documento: " + request.getDocumentoIdentidad());
        }

        huesped.setDocumentoIdentidad(request.getDocumentoIdentidad());
        huesped.setNombreCompleto(request.getNombreCompleto());
        huesped.setTelefono(request.getTelefono());
        huesped.setEmail(request.getEmail());
        huesped = huespedRepository.save(huesped);

        log.info("Huésped actualizado: {} (id={})", huesped.getNombreCompleto(), huesped.getId());
        return huespedMapper.toResponse(huesped);
    }

    /**
     * Finds an existing guest by documento or creates a new one.
     * Used internally by EstadiaService.
     *
     * @param request guest data
     * @return the Huesped entity (existing or newly created)
     */
    @Transactional
    public Huesped findOrCreate(HuespedRequest request) {
        Optional<Huesped> existing = huespedRepository
                .findByDocumentoIdentidad(request.getDocumentoIdentidad());

        if (existing.isPresent()) {
            log.debug("Huésped existente encontrado: {}", request.getDocumentoIdentidad());
            return existing.get();
        }

        Huesped huesped = huespedMapper.toEntity(request);
        huesped = huespedRepository.save(huesped);
        log.info("Huésped creado automáticamente: {} (id={})",
                huesped.getNombreCompleto(), huesped.getId());
        return huesped;
    }
}
