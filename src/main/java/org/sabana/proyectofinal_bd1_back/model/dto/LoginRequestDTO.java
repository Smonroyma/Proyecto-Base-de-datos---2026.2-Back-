package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la petición de inicio de sesión primario del Staff.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credenciales para el inicio de sesión del Staff")
public class LoginRequestDTO {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato de correo no es válido")
    @JsonAlias({"email"})
    @Schema(description = "Correo electrónico del miembro del staff", example = "admin@savanacoffee.com")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @JsonAlias({"password"})
    @Schema(description = "Contraseña del usuario", example = "Password123")
    private String contrasena;

}
