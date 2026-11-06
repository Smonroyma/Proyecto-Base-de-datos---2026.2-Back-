package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedido {

    private Long idPedido;
    private Long idProducto;
    private Integer cantidad;
    private Double precio;
    private String nombreProducto;

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

    public BigDecimal getPrecioBigDecimal() {
        return precio != null ? BigDecimal.valueOf(precio) : null;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio != null ? precio.doubleValue() : null;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }
}
