package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de entrada para la creación de un nuevo pedido en Savana Data Coffee.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos requeridos para la creación de un nuevo pedido")
public class PedidoRequestDTO {

    @NotNull(message = "El identificador del cliente es obligatorio")
    @Schema(description = "ID del cliente que realiza la comanda", example = "1")
    private Long idCliente;

    @Schema(description = "ID de la mesa asociada (obligatorio si el pedido es de tipo MESA, nulo si es para LLEVAR)", example = "3")
    private Long idMesa;

    @NotNull(message = "El identificador del usuario/empleado es obligatorio")
    @Schema(description = "ID del empleado o cajero que registra la orden", example = "2")
    private Long idUsuario;

    @NotBlank(message = "El tipo de pedido es obligatorio (MESA o LLEVAR)")
    @Schema(description = "Modalidad del pedido: MESA o LLEVAR", example = "MESA", allowableValues = {"MESA", "LLEVAR"})
    private String tipoPedido;

    @Schema(description = "Observaciones o instrucciones de preparación", example = "Café con leche deslactosada y poco azúcar")
    private String observaciones;

    @NotEmpty(message = "El pedido debe contener al menos un detalle de producto")
    @Valid
    @Schema(description = "Lista de productos y cantidades que componen el pedido")
    private List<DetallePedidoRequestDTO> detalles;

}
