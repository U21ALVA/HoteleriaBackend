package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.TurnoResponse;
import com.hoteleria.quantum.entity.Turno;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TurnoMapper {

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioNombre", source = "usuario.nombre")
    @Mapping(target = "estado", expression = "java(turno.getEstado() != null ? turno.getEstado().name() : null)")
    TurnoResponse toResponse(Turno turno);

    List<TurnoResponse> toResponseList(List<Turno> turnos);
}
