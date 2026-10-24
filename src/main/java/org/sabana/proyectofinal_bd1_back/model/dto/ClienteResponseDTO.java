package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida para presentar los datos de clientes al cliente API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con los datos detallados de un cliente")
public class ClienteResponseDTO {

    @Schema(description = "Identificador único del cliente", example = "1")
    private Long idCliente;

    @Schema(description = "Nombre del cliente", example = "Carlos")
    private String nombre;

    @Schema(description = "Apellido del cliente", example = "Mendoza")
    private String apellido;

    @Schema(description = "Correo electrónico único del cliente", example = "carlos.mendoza@email.com")
    private String correo;

    @Schema(description = "Número telefónico único de contacto", example = "+57 300 1234567")
    private String telefono;

}
