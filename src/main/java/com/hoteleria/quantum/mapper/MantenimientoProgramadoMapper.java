package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.MantenimientoProgramadoRequest;
import com.hoteleria.quantum.dto.MantenimientoProgramadoResponse;
import com.hoteleria.quantum.entity.MantenimientoProgramado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MantenimientoProgramadoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "habitacion", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "ultimaEjecucion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    MantenimientoProgramado toEntity(MantenimientoProgramadoRequest request);

    @Mapping(target = "habitacionId", expression = "java(mantenimientoProgramado.getHabitacion() != null ? mantenimientoProgramado.getHabitacion().getId() : null)")
    @Mapping(target = "habitacionNumero", expression = "java(mantenimientoProgramado.getHabitacion() != null ? mantenimientoProgramado.getHabitacion().getNumero() : null)")
    @Mapping(target = "categoriaId", expression = "java(mantenimientoProgramado.getCategoria() != null ? mantenimientoProgramado.getCategoria().getId() : null)")
    @Mapping(target = "categoriaNombre", expression = "java(mantenimientoProgramado.getCategoria() != null ? mantenimientoProgramado.getCategoria().getNombre() : null)")
    MantenimientoProgramadoResponse toResponse(MantenimientoProgramado mantenimientoProgramado);

    List<MantenimientoProgramadoResponse> toResponseList(List<MantenimientoProgramado> programados);
}
