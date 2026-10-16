package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Schema(description = "Nombre completo o razón social del cliente", example = "Carlos Mendoza")
    private String nombre;

    @Schema(description = "Número telefónico de contacto", example = "+57 300 1234567")
    private String telefono;

    @Schema(description = "Correo electrónico del cliente", example = "carlos.mendoza@email.com")
    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha y hora de registro en el sistema", example = "2026-10-16T10:30:00")
    private LocalDateTime fechaRegistro;

}
