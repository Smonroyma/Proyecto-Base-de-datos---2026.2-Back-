package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida para presentar los datos de mesas al cliente API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con los datos detallados de una mesa")
public class MesaResponseDTO {

    @Schema(description = "Número identificador único de la mesa en el salón (PK)", example = "5")
    private Integer numeroMesa;

    @Schema(description = "Capacidad de comensales sentados en la mesa", example = "4")
    private Integer capacidad;

    @Schema(description = "Estado operativo actual de la mesa", example = "DISPONIBLE", allowableValues = {"DISPONIBLE", "OCUPADA", "RESERVADA", "EN_MANTENIMIENTO"})
    private String estado;

}
