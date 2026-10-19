package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de entrada para la creación de un nuevo pedido en Savana Data Coffee.
 * Regla V2: Todo pedido es exclusivamente en mesa y requiere numeroMesa, idCliente e idUsuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos requeridos para la creación de un nuevo pedido en salón")
public class PedidoRequestDTO {

    @NotNull(message = "El identificador del cliente es obligatorio")
    @Schema(description = "ID del cliente que realiza la comanda", example = "1")
    private Long idCliente;

    @NotNull(message = "El número de la mesa es obligatorio (no existen pedidos para llevar)")
    @Min(value = 1, message = "El número de mesa debe ser mayor o igual a 1")
    @JsonAlias({"idMesa", "mesa"})
    @Schema(description = "Número único de la mesa asociada al pedido", example = "3")
    private Integer numeroMesa;

    @NotNull(message = "El identificador del empleado (Staff) es obligatorio")
    @Schema(description = "ID del empleado que registra la orden", example = "2")
    private Long idUsuario;

    @Schema(description = "Observaciones o instrucciones de preparación", example = "Café con leche deslactosada y poco azúcar")
    private String observaciones;

    @NotEmpty(message = "El pedido debe contener al menos un detalle de producto")
    @Valid
    @Schema(description = "Lista de productos y cantidades que componen el pedido")
    private List<DetallePedidoRequestDTO> detalles;

}
