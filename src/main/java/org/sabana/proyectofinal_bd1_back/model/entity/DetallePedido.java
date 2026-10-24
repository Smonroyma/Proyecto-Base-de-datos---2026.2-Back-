package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidad de dominio DetallePedido (POJO puro sin anotaciones JPA).
 * Representa la tabla asociativa 'detalle_pedido' con clave compuesta (id_pedido, id_producto) en SQL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedido {

    private Long idPedido;
    private Long idProducto;
    private Integer cantidad;
    private Double precioUnitario;
    private String nombreProducto;

    // Métodos alias para compatibilidad con pedidoId / productoId
    public Long getPedidoId() {
        return idPedido;
    }

    public void setPedidoId(Long pedidoId) {
        this.idPedido = pedidoId;
    }

    public Long getProductoId() {
        return idProducto;
    }

    public void setProductoId(Long productoId) {
        this.idProducto = productoId;
    }

    public BigDecimal getPrecioUnitarioBigDecimal() {
        return precioUnitario != null ? BigDecimal.valueOf(precioUnitario) : null;
    }

    public void setPrecioUnitario(BigDecimal precio) {
        this.precioUnitario = precio != null ? precio.doubleValue() : null;
    }

    public void setPrecioUnitario(Double precio) {
        this.precioUnitario = precio;
    }
}
