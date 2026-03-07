package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.CategoriaHabitacionRequest;
import com.hoteleria.quantum.dto.CategoriaHabitacionResponse;
import com.hoteleria.quantum.entity.CategoriaHabitacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoriaHabitacionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    @Mapping(target = "actualizadoEn", ignore = true)
    CategoriaHabitacion toEntity(CategoriaHabitacionRequest request);

    CategoriaHabitacionResponse toResponse(CategoriaHabitacion categoriaHabitacion);

    List<CategoriaHabitacionResponse> toResponseList(List<CategoriaHabitacion> categorias);
}
