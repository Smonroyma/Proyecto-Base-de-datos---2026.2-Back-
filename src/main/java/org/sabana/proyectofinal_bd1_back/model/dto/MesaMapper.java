package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;

import java.util.List;

/**
 * Mapper MapStruct para la conversión entre Mesa y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface MesaMapper {

    Mesa toEntity(MesaRequestDTO dto);

    MesaResponseDTO toResponseDTO(Mesa entity);

    List<MesaResponseDTO> toResponseDTOList(List<Mesa> entities);

    void updateEntityFromDTO(MesaRequestDTO dto, @MappingTarget Mesa entity);

}
