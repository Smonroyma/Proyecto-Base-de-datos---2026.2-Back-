package org.sabana.proyectofinal_bd1_back.model.dto;

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
 * DTO para el registro o actualización de un usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para el registro o actualización de un usuario")
public class UsuarioRequestDTO {

    @NotBlank(message = "El nombre del usuario es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre completo del usuario", example = "Sofía Monroy")
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 100, message = "El correo electrónico no puede superar los 100 caracteres")
    @Schema(description = "Correo electrónico institucional o de acceso", example = "sofia.monroy@savanacoffee.com")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    @Schema(description = "Contraseña en texto plano para registro", example = "Segura123*")
    private String password;

    @NotBlank(message = "El rol del usuario es obligatorio")
    @Pattern(
            regexp = "^(ADMINISTRADOR|BARISTA|CAJERO)$",
            message = "El rol debe ser uno de los permitidos: ADMINISTRADOR, BARISTA, CAJERO"
    )
    @Schema(description = "Rol operativo del usuario", example = "ADMINISTRADOR", allowableValues = {"ADMINISTRADOR", "BARISTA", "CAJERO"})
    private String rol;

    @Schema(description = "Indica si el usuario tiene habilitada la autenticación en dos factores (2FA)", example = "true", defaultValue = "false")
    private Boolean dosFactoresHabilitado;

}
