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
 * Mapea directamente la tabla de base de datos 'factura' sin anotaciones JPA.
 * Regla V2: num_factura (String) es la PK, el impuesto es del 19% (IVA) y no existe descuento.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Factura {

    private String numFactura;
    private Long idPedido;
    private Long idCliente;
    private String nombreCliente;
    private String correoCliente;
    private String telefonoCliente;
    private Long idUsuario;
    private String nombreUsuario;
    private LocalDateTime fecha;
    private String estado;
    private Double subtotal;
    private Double propina;
    private Double impuesto;
    private Double total;

    @Builder.Default
    private List<PagoFactura> pagos = new ArrayList<>();

}
