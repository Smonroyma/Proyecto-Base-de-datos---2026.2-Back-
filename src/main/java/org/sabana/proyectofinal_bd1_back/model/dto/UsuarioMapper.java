package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.sabana.proyectofinal_bd1_back.model.entity.Usuario;

import java.util.List;

/**
 * Mapper MapStruct para conversiones entre Usuario y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "codigoOtp", ignore = true)
    @Mapping(target = "expiracionOtp", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    Usuario toEntity(UsuarioRequestDTO dto);

    UsuarioResponseDTO toResponseDTO(Usuario entity);

    List<UsuarioResponseDTO> toResponseDTOList(List<Usuario> entities);

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "codigoOtp", ignore = true)
    @Mapping(target = "expiracionOtp", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    void updateEntityFromDTO(UsuarioRequestDTO dto, @MappingTarget Usuario entity);

}
