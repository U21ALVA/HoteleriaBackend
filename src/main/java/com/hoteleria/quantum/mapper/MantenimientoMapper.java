package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.MantenimientoResponse;
import com.hoteleria.quantum.entity.Mantenimiento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MantenimientoMapper {

    @Mapping(target = "habitacionId", source = "habitacion.id")
    @Mapping(target = "habitacionNumero", source = "habitacion.numero")
    @Mapping(target = "tipo", expression = "java(mantenimiento.getTipo() != null ? mantenimiento.getTipo().name() : null)")
    @Mapping(target = "usuarioReportaId", source = "usuarioReporta.id")
    @Mapping(target = "usuarioReportaNombre", source = "usuarioReporta.nombre")
    MantenimientoResponse toResponse(Mantenimiento mantenimiento);

    List<MantenimientoResponse> toResponseList(List<Mantenimiento> mantenimientos);
}
