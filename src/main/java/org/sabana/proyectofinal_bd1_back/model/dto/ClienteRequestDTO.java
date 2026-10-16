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
 * DTO de entrada para la creación y actualización de clientes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para el registro o actualización de un cliente")
public class ClienteRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre completo o razón social del cliente", example = "Carlos Mendoza")
    private String nombre;

    @NotBlank(message = "El teléfono del cliente es obligatorio")
    @Pattern(regexp = "^[0-9+()\\s-]{7,20}$", message = "El número telefónico contiene caracteres no válidos o longitud incorrecta")
    @Schema(description = "Número telefónico de contacto", example = "+57 300 1234567")
    private String telefono;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 150, message = "El correo electrónico no puede superar los 150 caracteres")
    @Schema(description = "Correo electrónico del cliente", example = "carlos.mendoza@email.com")
    private String email;

}
