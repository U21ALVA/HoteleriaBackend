package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.HuespedRequest;
import com.hoteleria.quantum.dto.HuespedResponse;
import com.hoteleria.quantum.entity.Huesped;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HuespedMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    @Mapping(target = "actualizadoEn", ignore = true)
    Huesped toEntity(HuespedRequest request);

    HuespedResponse toResponse(Huesped huesped);

    List<HuespedResponse> toResponseList(List<Huesped> huespedes);
}
