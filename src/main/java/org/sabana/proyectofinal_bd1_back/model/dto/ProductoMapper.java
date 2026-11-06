package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.sabana.proyectofinal_bd1_back.model.entity.Producto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    @Mapping(target = "idProducto", ignore = true)
    Producto toEntity(ProductoRequestDTO dto);

    ProductoResponseDTO toResponseDTO(Producto entity);

    List<ProductoResponseDTO> toResponseDTOList(List<Producto> entities);

    @Mapping(target = "idProducto", ignore = true)
    void updateEntityFromDTO(ProductoRequestDTO dto, @MappingTarget Producto entity);

}
