package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sabana.proyectofinal_bd1_back.model.entity.DetallePedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Mapper(componentModel = "spring")
public interface DetallePedidoMapper {

    @Mapping(target = "idPedido", ignore = true)
    @Mapping(target = "nombreProducto", ignore = true)
    @Mapping(target = "precio", ignore = true)
    DetallePedido toEntity(DetallePedidoRequestDTO dto);

    default DetallePedidoResponseDTO toResponseDTO(DetallePedido entity) {
        if (entity == null) {
            return null;
        }
        String compositeId = null;
        if (entity.getIdPedido() != null && entity.getIdProducto() != null) {
            compositeId = entity.getIdPedido() + "_" + entity.getIdProducto();
        }

        Double precio = entity.getPrecio();
        String nombreProd = entity.getNombreProducto();

        Double subtotalCalculado = 0.0;
        if (precio != null && entity.getCantidad() != null) {
            subtotalCalculado = BigDecimal.valueOf(precio)
                    .multiply(BigDecimal.valueOf(entity.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return DetallePedidoResponseDTO.builder()
                .idDetallePedido(compositeId)
                .idProducto(entity.getIdProducto())
                .nombreProducto(nombreProd)
                .cantidad(entity.getCantidad())
                .precioUnitario(precio)
                .subtotal(subtotalCalculado)
                .build();
    }

    List<DetallePedidoResponseDTO> toResponseDTOList(List<DetallePedido> entities);

}
