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
@Schema(description = "Respuesta detallada de un ítem de producto en el pedido")
public class DetallePedidoResponseDTO {

    @Schema(description = "Identificador compuesto del detalle de pedido (idPedido_idProducto)", example = "1_1")
    private String idDetallePedido;

    @Schema(description = "ID del producto ordenado", example = "1")
    private Long idProducto;

    @Schema(description = "Nombre legible del producto", example = "Café Americano")
    private String nombreProducto;

    @Schema(description = "Cantidad ordenada", example = "2")
    private Integer cantidad;

    @Schema(description = "Precio unitario del producto", example = "6500.0")
    private Double precioUnitario;

    @Schema(description = "Subtotal calculado dinámicamente para este ítem (cantidad * precioUnitario)", example = "13000.0")
    private Double subtotal;

    public Long getProductoId() {
        return idProducto;
    }

    public void setProductoId(Long productoId) {
        this.idProducto = productoId;
    }

}
