package com.hoteleria.quantum.mapper;

import com.hoteleria.quantum.dto.LogAuditoriaResponse;
import com.hoteleria.quantum.entity.LogAuditoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LogAuditoriaMapper {

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioNombre", source = "usuario.nombre")
    LogAuditoriaResponse toResponse(LogAuditoria logAuditoria);

    List<LogAuditoriaResponse> toResponseList(List<LogAuditoria> logs);
}
