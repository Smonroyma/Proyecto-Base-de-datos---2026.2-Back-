package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para la validación del código 2FA/OTP")
public class Verificar2FARequestDTO {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato de correo no es válido")
    @JsonAlias({"email"})
    @Schema(description = "Correo electrónico del usuario que solicita verificación", example = "admin@savanacoffee.com")
    private String correo;

    @NotBlank(message = "El código OTP es obligatorio")
    @Pattern(regexp = "^[0-9]{6}$", message = "El código OTP debe componerse exactamente de 6 dígitos numéricos")
    @Schema(description = "Código de 6 dígitos enviado al correo", example = "482910")
    private String codigoOtp;

}
