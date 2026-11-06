package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos públicos del miembro de Staff")
public class UsuarioResponseDTO {

    @Schema(description = "Identificador único del usuario (staff)", example = "1")
    private Long idUsuario;

    @Schema(description = "Nombres del usuario", example = "Sofía")
    private String nombres;

    @Schema(description = "Apellidos del usuario", example = "Monroy")
    private String apellidos;

    @Schema(description = "Correo electrónico del usuario", example = "sofia.monroy@savanacoffee.com")
    private String correo;

    @Schema(description = "Rol operativo según jerarquía de Staff", example = "MESERO", allowableValues = {"ADMINISTRADOR", "MESERO"})
    private String rol;

    @Schema(description = "Estado operativo del mesero (ACTIVO / INACTIVO)", example = "ACTIVO")
    private String estado;

    @Schema(description = "Indica si el usuario tiene activado 2FA", example = "true")
    private Boolean dosFacHab;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha de inicio en la plataforma", example = "2026-10-16")
    private LocalDate fechaInicio;

    @Schema(description = "Nombre completo concatenado para visualización", example = "Sofía Monroy")
    public String getNombre() {
        String n = nombres != null ? nombres.trim() : "";
        String a = apellidos != null ? apellidos.trim() : "";
        return (n + " " + a).trim();
    }

    @Schema(description = "Alias de correo electrónico para compatibilidad", example = "sofia.monroy@savanacoffee.com")
    public String getEmail() {
        return correo;
    }

    @Schema(description = "Indica si el miembro del staff se encuentra activo", example = "true")
    public Boolean getActivo() {
        return estado == null || "ACTIVO".equalsIgnoreCase(estado.trim());
    }

    @Schema(description = "Alias de dosFacHab para compatibilidad", example = "true")
    public Boolean getDosFactoresHabilitado() {
        return dosFacHab;
    }

}
