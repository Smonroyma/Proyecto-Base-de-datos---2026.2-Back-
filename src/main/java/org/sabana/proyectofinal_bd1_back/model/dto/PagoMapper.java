package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sabana.proyectofinal_bd1_back.model.entity.Pago;

import java.util.List;

/**
 * Mapper MapStruct para transformar entre Pago y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface PagoMapper {

    @Mapping(target = "idPago", ignore = true)
    @Mapping(target = "numFactura", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "estado", ignore = true)
    Pago toEntity(PagoRequestDTO dto);

    PagoResponseDTO toResponseDTO(Pago entity);

    List<PagoResponseDTO> toResponseDTOList(List<Pago> entities);

}
