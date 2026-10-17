package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.sabana.proyectofinal_bd1_back.model.entity.Categoria;

import java.util.List;

/**
 * Mapper MapStruct para transformar entre Categoria y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    @Mapping(target = "idCategoria", ignore = true)
    Categoria toEntity(CategoriaRequestDTO dto);

    CategoriaResponseDTO toResponseDTO(Categoria entity);

    List<CategoriaResponseDTO> toResponseDTOList(List<Categoria> entities);

    @Mapping(target = "idCategoria", ignore = true)
    void updateEntityFromDTO(CategoriaRequestDTO dto, @MappingTarget Categoria entity);

}
