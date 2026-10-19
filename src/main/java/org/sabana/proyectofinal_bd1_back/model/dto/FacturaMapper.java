package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sabana.proyectofinal_bd1_back.model.entity.Factura;

import java.util.List;

/**
 * Mapper MapStruct para transformar entre Factura y sus DTOs.
 */
@Mapper(componentModel = "spring", uses = {PagoMapper.class})
public interface FacturaMapper {

    @Mapping(target = "numFactura", ignore = true)
    @Mapping(target = "idCliente", ignore = true)
    @Mapping(target = "nombreCliente", ignore = true)
    @Mapping(target = "correoCliente", ignore = true)
    @Mapping(target = "telefonoCliente", ignore = true)
    @Mapping(target = "nombreUsuario", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "impuesto", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "pagos", ignore = true)
    Factura toEntity(FacturaRequestDTO dto);

    FacturaResponseDTO toResponseDTO(Factura entity);

    List<FacturaResponseDTO> toResponseDTOList(List<Factura> entities);

}
