package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.sabana.proyectofinal_bd1_back.model.entity.Reserva;

import java.util.List;

/**
 * Mapper MapStruct para la transformación entre Reserva y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface ReservaMapper {

    @Mapping(target = "idReserva", ignore = true)
    @Mapping(target = "nombreCliente", ignore = true)
    @Mapping(target = "numeroMesa", ignore = true)
    Reserva toEntity(ReservaRequestDTO dto);

    ReservaResponseDTO toResponseDTO(Reserva entity);

    List<ReservaResponseDTO> toResponseDTOList(List<Reserva> entities);

    @Mapping(target = "idReserva", ignore = true)
    @Mapping(target = "nombreCliente", ignore = true)
    @Mapping(target = "numeroMesa", ignore = true)
    void updateEntityFromDTO(ReservaRequestDTO dto, @MappingTarget Reserva entity);

}
