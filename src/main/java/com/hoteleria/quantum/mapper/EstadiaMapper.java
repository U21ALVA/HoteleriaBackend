package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.EstadiaResponse;
import com.hoteleria.quantum.entity.Estadia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EstadiaMapper {

    @Mapping(target = "huespedId", source = "huesped.id")
    @Mapping(target = "huespedNombre", source = "huesped.nombreCompleto")
    @Mapping(target = "huespedDocumento", source = "huesped.documentoIdentidad")
    @Mapping(target = "usuarioRegistroId", source = "usuarioRegistro.id")
    @Mapping(target = "usuarioRegistroNombre", source = "usuarioRegistro.nombre")
    @Mapping(target = "estado", expression = "java(estadia.getEstado() != null ? estadia.getEstado().name() : null)")
    @Mapping(target = "origen", expression = "java(estadia.getOrigen() != null ? estadia.getOrigen().name() : null)")
    @Mapping(target = "depositoDevuelto", ignore = true)
    @Mapping(target = "habitaciones", ignore = true)
    @Mapping(target = "pagos", ignore = true)
    @Mapping(target = "saldoPendiente", ignore = true)
    EstadiaResponse toResponse(Estadia estadia);

    List<EstadiaResponse> toResponseList(List<Estadia> estadias);
}
