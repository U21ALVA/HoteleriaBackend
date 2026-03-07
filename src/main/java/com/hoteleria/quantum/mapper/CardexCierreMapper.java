package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.CardexCierreResponse;
import com.hoteleria.quantum.entity.CardexCierre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardexCierreMapper {

    @Mapping(target = "turnoId", source = "turno.id")
    @Mapping(target = "usuarioCierreId", source = "usuarioCierre.id")
    @Mapping(target = "usuarioCierreNombre", source = "usuarioCierre.nombre")
    CardexCierreResponse toResponse(CardexCierre cardexCierre);

    List<CardexCierreResponse> toResponseList(List<CardexCierre> cierres);
}
