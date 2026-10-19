package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el registro o actualización de un miembro de Staff (Administrador o Empleado).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para el registro o actualización de un miembro del Staff")
public class UsuarioRequestDTO {

    @NotBlank(message = "Los nombres del usuario son obligatorios")
    @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
    @JsonAlias({"nombre"})
    @Schema(description = "Nombres del miembro del staff", example = "Sofía")
    private String nombres;

    @Builder.Default
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    @Schema(description = "Apellidos del miembro del staff", example = "Monroy")
    private String apellidos = "";

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 150, message = "El correo electrónico no puede superar los 150 caracteres")
    @JsonAlias({"email"})
    @Schema(description = "Correo electrónico institucional o de acceso", example = "sofia.monroy@savanacoffee.com")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    @JsonAlias({"password"})
    @Schema(description = "Contraseña en texto plano para registro", example = "Segura123*")
    private String contrasena;

    @NotBlank(message = "El rol del usuario es obligatorio")
    @Pattern(
            regexp = "^(?i)(ADMINISTRADOR|EMPLEADO)$",
            message = "El rol debe ser uno de los permitidos: ADMINISTRADOR, EMPLEADO"
    )
    @Schema(description = "Rol operativo del miembro de staff", example = "EMPLEADO", allowableValues = {"ADMINISTRADOR", "EMPLEADO"})
    private String rol;

    @JsonAlias({"dosFactoresHabilitado"})
    @Schema(description = "Indica si el usuario tiene habilitada la autenticación en dos factores (2FA)", example = "true", defaultValue = "false")
    private Boolean dosFacHab;

    @Pattern(
            regexp = "^(?i)(ACTIVO|INACTIVO)$",
            message = "El estado del empleado debe ser ACTIVO o INACTIVO"
    )
    @Schema(description = "Estado operativo del empleado (solo aplica para rol EMPLEADO)", example = "ACTIVO", allowableValues = {"ACTIVO", "INACTIVO"})
    private String estado;

}
