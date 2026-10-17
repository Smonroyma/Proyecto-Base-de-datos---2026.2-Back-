package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para operaciones de autenticación (Login, Registro y 2FA).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta de autenticación con token JWT o requerimiento 2FA")
public class AuthResponseDTO {

    @Schema(description = "Token JWT de acceso (null si requiere2FA es true)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Tipo de token para cabecera Authorization", example = "Bearer")
    private String tipoToken;

    @Schema(description = "Indica si se requiere validar el segundo factor OTP", example = "false")
    private Boolean requiere2FA;

    @Schema(description = "Mensaje informativo sobre el resultado de la autenticación", example = "Inicio de sesión exitoso")
    private String mensaje;

    @Schema(description = "Información del usuario autenticado")
    private UsuarioResponseDTO usuario;

}
