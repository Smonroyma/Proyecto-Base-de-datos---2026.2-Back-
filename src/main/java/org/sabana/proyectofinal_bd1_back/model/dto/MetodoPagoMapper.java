package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.sabana.proyectofinal_bd1_back.model.entity.MetodoPago;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MetodoPagoMapper {

    MetodoPago toEntity(MetodoPagoRequestDTO dto);

    MetodoPagoResponseDTO toResponseDTO(MetodoPago entity);

    List<MetodoPagoResponseDTO> toResponseDTOList(List<MetodoPago> entities);

    void updateEntityFromDTO(MetodoPagoRequestDTO dto, @MappingTarget MetodoPago entity);

}
