package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sabana.proyectofinal_bd1_back.model.entity.PagoFactura;

import java.util.List;

/**
 * Mapper MapStruct para transformar entre PagoFactura y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface PagoMapper {

    @Mapping(target = "idPago", ignore = true)
    @Mapping(target = "idFactura", ignore = true)
    @Mapping(target = "nombreMetodoPago", ignore = true)
    @Mapping(target = "fechaHora", ignore = true)
    PagoFactura toEntity(PagoRequestDTO dto);

    PagoResponseDTO toResponseDTO(PagoFactura entity);

    List<PagoResponseDTO> toResponseDTOList(List<PagoFactura> entities);

}
