package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.ConfiguracionResponse;
import com.hoteleria.quantum.entity.ConfiguracionHotel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConfiguracionMapper {

    @Mapping(target = "actualizadoEn", source = "actualizadoEn")
    ConfiguracionResponse toResponse(ConfiguracionHotel configuracionHotel);

    List<ConfiguracionResponse> toResponseList(List<ConfiguracionHotel> configuraciones);
}
