package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad de dominio DetallePedido.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedido {

    private Long idDetallePedido;
    private Long idPedido;
    private Long idProducto;
    private String nombreProducto;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;

}
