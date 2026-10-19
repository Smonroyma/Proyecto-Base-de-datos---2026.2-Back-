package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida con la información estructurada de un método de pago.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con la información de un método de pago")
public class MetodoPagoResponseDTO {

    @Schema(description = "Tipo único del método de pago (PK)", example = "EFECTIVO")
    private String tipo;

    @Schema(description = "Nombre visible del método de pago", example = "Efectivo")
    private String nombre;

    @Schema(description = "Descripción del método de pago", example = "Pago en moneda local en caja")
    private String descripcion;

    @Schema(description = "Indica si el método de pago está activo", example = "true")
    private Boolean activo;

}
