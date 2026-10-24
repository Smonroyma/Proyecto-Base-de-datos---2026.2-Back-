package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;

import java.util.List;

/**
 * Mapper MapStruct para la conversión entre Cliente y sus respectivos DTOs.
 */
@Mapper(componentModel = "spring")
public interface ClienteMapper {

    @Mapping(target = "idCliente", ignore = true)
    Cliente toEntity(ClienteRequestDTO dto);

    ClienteResponseDTO toResponseDTO(Cliente entity);

    List<ClienteResponseDTO> toResponseDTOList(List<Cliente> entities);

    @Mapping(target = "idCliente", ignore = true)
    void updateEntityFromDTO(ClienteRequestDTO dto, @MappingTarget Cliente entity);

}
