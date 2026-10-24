package org.sabana.proyectofinal_bd1_back.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.sabana.proyectofinal_bd1_back.model.entity.Administrador;
import org.sabana.proyectofinal_bd1_back.model.entity.Mesero;
import org.sabana.proyectofinal_bd1_back.model.entity.Staff;

import java.time.LocalDate;
import java.util.List;

/**
 * Mapper MapStruct para conversiones entre la jerarquía Staff (Administrador / Mesero) y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    default Staff toEntity(UsuarioRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        String rolNormalizado = dto.getRol() != null ? dto.getRol().trim().toUpperCase() : "MESERO";
        boolean dosFac = Boolean.TRUE.equals(dto.getDosFacHab());
        String apellidos = dto.getApellidos() != null ? dto.getApellidos().trim() : "";

        if ("ADMINISTRADOR".equals(rolNormalizado)) {
            return Administrador.builder()
                    .nombres(dto.getNombres() != null ? dto.getNombres().trim() : null)
                    .apellidos(apellidos)
                    .correo(dto.getCorreo() != null ? dto.getCorreo().trim().toLowerCase() : null)
                    .dosFacHab(dosFac)
                    .fechaInicio(LocalDate.now())
                    .rol("ADMINISTRADOR")
                    .build();
        } else {
            String estado = (dto.getEstado() != null && !dto.getEstado().isBlank())
                    ? dto.getEstado().trim().toUpperCase()
                    : "ACTIVO";
            return Mesero.builder()
                    .nombres(dto.getNombres() != null ? dto.getNombres().trim() : null)
                    .apellidos(apellidos)
                    .correo(dto.getCorreo() != null ? dto.getCorreo().trim().toLowerCase() : null)
                    .dosFacHab(dosFac)
                    .fechaInicio(LocalDate.now())
                    .rol("MESERO")
                    .estado(estado)
                    .build();
        }
    }

    default UsuarioResponseDTO toResponseDTO(Staff entity) {
        if (entity == null) {
            return null;
        }
        String estado = "ACTIVO";
        if (entity instanceof Mesero mesero && mesero.getEstado() != null) {
            estado = mesero.getEstado();
        }
        return UsuarioResponseDTO.builder()
                .idUsuario(entity.getIdUsuario())
                .nombres(entity.getNombres())
                .apellidos(entity.getApellidos())
                .correo(entity.getCorreo())
                .rol(entity.getRol())
                .estado(estado)
                .dosFacHab(entity.getDosFacHab())
                .fechaInicio(entity.getFechaInicio())
                .build();
    }

    List<UsuarioResponseDTO> toResponseDTOList(List<Staff> entities);

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "contrasena", ignore = true)
    @Mapping(target = "fechaInicio", ignore = true)
    void updateEntityFromDTO(UsuarioRequestDTO dto, @MappingTarget Staff entity);

}
