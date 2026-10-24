package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sabana.proyectofinal_bd1_back.model.entity.Pedido;
import org.sabana.proyectofinal_bd1_back.util.SubtotalCalculator;

import java.util.List;

/**
 * Mapper MapStruct para transformaciones entre Pedido y sus DTOs.
 */
@Mapper(componentModel = "spring", uses = {DetallePedidoMapper.class})
public interface PedidoMapper {

    @Mapping(target = "idPedido", ignore = true)
    @Mapping(target = "nombreCliente", ignore = true)
    @Mapping(target = "nombreUsuario", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    Pedido toEntity(PedidoRequestDTO dto);

    @Mapping(target = "subtotal", expression = "java(calcularSubtotal(entity))")
    PedidoResponseDTO toResponseDTO(Pedido entity);

    List<PedidoResponseDTO> toResponseDTOList(List<Pedido> entities);

    default Double calcularSubtotal(Pedido entity) {
        return SubtotalCalculator.calcularSubtotal(entity);
    }

}
