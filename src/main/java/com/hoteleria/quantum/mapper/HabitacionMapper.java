package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.HabitacionRequest;
import com.hoteleria.quantum.dto.HabitacionResponse;
import com.hoteleria.quantum.entity.Habitacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HabitacionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    @Mapping(target = "actualizadoEn", ignore = true)
    Habitacion toEntity(HabitacionRequest request);

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "precioBase", source = "categoria.precioBase")
    @Mapping(target = "capacidad", source = "categoria.capacidad")
    @Mapping(target = "estado", expression = "java(habitacion.getEstado() != null ? habitacion.getEstado().name() : null)")
    HabitacionResponse toResponse(Habitacion habitacion);

    List<HabitacionResponse> toResponseList(List<Habitacion> habitaciones);
}
