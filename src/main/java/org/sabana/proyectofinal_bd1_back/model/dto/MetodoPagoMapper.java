package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.sabana.proyectofinal_bd1_back.model.entity.MetodoPago;

import java.util.List;

/**
 * Mapper MapStruct para transformar entre MetodoPago y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface MetodoPagoMapper {

    @Mapping(target = "idMetodoPago", ignore = true)
    MetodoPago toEntity(MetodoPagoRequestDTO dto);

    MetodoPagoResponseDTO toResponseDTO(MetodoPago entity);

    List<MetodoPagoResponseDTO> toResponseDTOList(List<MetodoPago> entities);

    @Mapping(target = "idMetodoPago", ignore = true)
    void updateEntityFromDTO(MetodoPagoRequestDTO dto, @MappingTarget MetodoPago entity);

}
