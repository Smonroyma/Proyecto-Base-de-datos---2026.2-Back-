package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para presentar información pública del usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos públicos del usuario")
public class UsuarioResponseDTO {

    @Schema(description = "Identificador único del usuario", example = "1")
    private Long idUsuario;

    @Schema(description = "Nombre completo del usuario", example = "Sofía Monroy")
    private String nombre;

    @Schema(description = "Correo electrónico del usuario", example = "sofia.monroy@savanacoffee.com")
    private String email;

    @Schema(description = "Rol operativo del usuario", example = "ADMINISTRADOR")
    private String rol;

    @Schema(description = "Indica si el usuario se encuentra activo en el sistema", example = "true")
    private Boolean activo;

    @Schema(description = "Indica si el usuario tiene activado 2FA", example = "true")
    private Boolean dosFactoresHabilitado;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de registro en la plataforma", example = "2026-10-16T18:00:00")
    private LocalDateTime fechaRegistro;

}
