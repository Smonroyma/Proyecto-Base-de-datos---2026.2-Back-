package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con la información de un método de pago")
public class MetodoPagoResponseDTO {

    @Schema(description = "Tipo único del método de pago (PK)", example = "EFECTIVO")
    private String tipo;

}
