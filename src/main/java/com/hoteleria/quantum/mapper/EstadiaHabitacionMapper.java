package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.EstadiaHabitacionResponse;
import com.hoteleria.quantum.entity.EstadiaHabitacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EstadiaHabitacionMapper {

    @Mapping(target = "habitacionId", source = "habitacion.id")
    @Mapping(target = "habitacionNumero", source = "habitacion.numero")
    @Mapping(target = "habitacionPiso", source = "habitacion.piso")
    @Mapping(target = "categoriaNombre", source = "habitacion.categoria.nombre")
    EstadiaHabitacionResponse toResponse(EstadiaHabitacion estadiaHabitacion);

    List<EstadiaHabitacionResponse> toResponseList(List<EstadiaHabitacion> estadiaHabitaciones);
}
