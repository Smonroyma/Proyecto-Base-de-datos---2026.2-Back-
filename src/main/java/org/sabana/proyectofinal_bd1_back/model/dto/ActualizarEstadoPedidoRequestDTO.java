package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la actualización de estado de un pedido.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para modificar el estado de un pedido")
public class ActualizarEstadoPedidoRequestDTO {

    @NotBlank(message = "El nuevo estado del pedido es obligatorio")
    @JsonAlias({"estado", "nuevo_estado"})
    @Schema(description = "Nuevo estado a asignar al pedido", example = "EN_PREPARACION", allowableValues = {"PENDIENTE", "EN_PREPARACION", "ENTREGADO", "CANCELADO", "PAGADO"})
    private String nuevoEstado;

}
