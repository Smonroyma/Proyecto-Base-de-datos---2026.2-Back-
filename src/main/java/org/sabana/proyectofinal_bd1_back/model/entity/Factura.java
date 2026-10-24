package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio Factura.
 * POJO puro sin anotaciones de persistencia JPA.
 * Regla V3: No tiene subtotal, ni idUsuario, ni idCliente directos en BD (se resuelven a través del Pedido).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Factura {

    private String numFactura;
    private LocalDateTime fecha;
    private String estado;
    private Double total;
    private Double propina;
    private Double impuesto;
    private Long idPedido;

    private String nombreCliente;
    private String correoCliente;
    private String telefonoCliente;
    private String nombreUsuario;

    @Builder.Default
    private List<Pago> pagos = new ArrayList<>();

}
