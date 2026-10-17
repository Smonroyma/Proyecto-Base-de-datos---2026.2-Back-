package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de salida con los datos completos y estructurados de un pedido.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta detallada con la información de un pedido y sus detalles")
public class PedidoResponseDTO {

    @Schema(description = "Identificador único del pedido", example = "50")
    private Long idPedido;

    @Schema(description = "ID del cliente asociado", example = "1")
    private Long idCliente;

    @Schema(description = "Nombre completo del cliente", example = "Carlos Mendoza")
    private String nombreCliente;

    @Schema(description = "ID de la mesa asociada (nulo si es para llevar)", example = "3")
    private Long idMesa;

    @Schema(description = "Número visible de la mesa asignada", example = "5")
    private Integer numeroMesa;

    @Schema(description = "ID del usuario/empleado que atendió", example = "2")
    private Long idUsuario;

    @Schema(description = "Nombre del empleado que atendió la orden", example = "Ana Gómez")
    private String nombreUsuario;

    @Schema(description = "Fecha y hora exacta de registro del pedido", example = "2026-10-17T10:15:30")
    private LocalDateTime fechaHora;

    @Schema(description = "Estado actual del pedido", example = "PENDIENTE", allowableValues = {"PENDIENTE", "EN_PREPARACION", "ENTREGADO", "CANCELADO", "PAGADO"})
    private String estado;

    @Schema(description = "Modalidad del pedido", example = "MESA", allowableValues = {"MESA", "LLEVAR"})
    private String tipoPedido;

    @Schema(description = "Monto total acumulado del pedido", example = "28500.0")
    private Double total;

    @Schema(description = "Observaciones o instrucciones de la orden", example = "Café con leche deslactosada y poco azúcar")
    private String observaciones;

    @Builder.Default
    @Schema(description = "Lista detallada de los ítems de productos que componen el pedido")
    private List<DetallePedidoResponseDTO> detalles = new ArrayList<>();

}
