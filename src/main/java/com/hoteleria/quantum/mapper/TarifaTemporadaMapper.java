package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.TarifaTemporadaRequest;
import com.hoteleria.quantum.dto.TarifaTemporadaResponse;
import com.hoteleria.quantum.entity.TarifaTemporada;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TarifaTemporadaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    TarifaTemporada toEntity(TarifaTemporadaRequest request);

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    TarifaTemporadaResponse toResponse(TarifaTemporada tarifaTemporada);

    List<TarifaTemporadaResponse> toResponseList(List<TarifaTemporada> tarifas);
}
