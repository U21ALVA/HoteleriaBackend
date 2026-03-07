package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.PagoResponse;
import com.hoteleria.quantum.entity.Pago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PagoMapper {

    @Mapping(target = "estadiaId", source = "estadia.id")
    @Mapping(target = "estadiaCodigo", source = "estadia.codigo")
    @Mapping(target = "metodoPago", expression = "java(pago.getMetodoPago() != null ? pago.getMetodoPago().name() : null)")
    @Mapping(target = "usuarioRegistroId", source = "usuarioRegistro.id")
    @Mapping(target = "usuarioRegistroNombre", source = "usuarioRegistro.nombre")
    @Mapping(target = "turnoId", source = "turno.id")
    PagoResponse toResponse(Pago pago);

    List<PagoResponse> toResponseList(List<Pago> pagos);
}
