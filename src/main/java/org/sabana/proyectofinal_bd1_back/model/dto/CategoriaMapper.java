package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.sabana.proyectofinal_bd1_back.model.entity.Categoria;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    Categoria toEntity(CategoriaRequestDTO dto);

    CategoriaResponseDTO toResponseDTO(Categoria entity);

    List<CategoriaResponseDTO> toResponseDTOList(List<Categoria> entities);

    void updateEntityFromDTO(CategoriaRequestDTO dto, @MappingTarget Categoria entity);

}
