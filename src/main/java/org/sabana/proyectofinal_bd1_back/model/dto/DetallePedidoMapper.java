package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sabana.proyectofinal_bd1_back.model.entity.DetallePedido;

import java.util.List;

/**
 * Mapper MapStruct para transformaciones entre DetallePedido y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface DetallePedidoMapper {

    @Mapping(target = "idDetallePedido", ignore = true)
    @Mapping(target = "idPedido", ignore = true)
    @Mapping(target = "nombreProducto", ignore = true)
    @Mapping(target = "precioUnitario", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    DetallePedido toEntity(DetallePedidoRequestDTO dto);

    DetallePedidoResponseDTO toResponseDTO(DetallePedido entity);

    List<DetallePedidoResponseDTO> toResponseDTOList(List<DetallePedido> entities);

}
