package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.CajaMovimientoResponse;
import com.hoteleria.quantum.entity.CajaMovimiento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CajaMovimientoMapper {

    @Mapping(target = "tipo", expression = "java(cajaMovimiento.getTipo() != null ? cajaMovimiento.getTipo().name() : null)")
    @Mapping(target = "metodoPago", expression = "java(cajaMovimiento.getMetodoPago() != null ? cajaMovimiento.getMetodoPago().name() : null)")
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioNombre", source = "usuario.nombre")
    @Mapping(target = "turnoId", source = "turno.id")
    @Mapping(target = "estadiaId", expression = "java(cajaMovimiento.getEstadia() != null ? cajaMovimiento.getEstadia().getId() : null)")
    @Mapping(target = "estadiaCodigo", expression = "java(cajaMovimiento.getEstadia() != null ? cajaMovimiento.getEstadia().getCodigo() : null)")
    @Mapping(target = "pagoId", expression = "java(cajaMovimiento.getPago() != null ? cajaMovimiento.getPago().getId() : null)")
    CajaMovimientoResponse toResponse(CajaMovimiento cajaMovimiento);

    List<CajaMovimientoResponse> toResponseList(List<CajaMovimiento> movimientos);
}
