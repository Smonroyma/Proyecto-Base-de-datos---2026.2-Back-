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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Factura {

    private Long idFactura;
    private String numeroFactura;
    private Long idPedido;
    private Long idCliente;
    private String nombreCliente;
    private String identificacionCliente;
    private Long idUsuario;
    private String nombreUsuario;
    private LocalDateTime fechaEmision;
    private Double subtotal;
    private Double propina;
    private Double descuento;
    private Double impuesto;
    private Double total;
    private String estado;

    @Builder.Default
    private List<PagoFactura> pagos = new ArrayList<>();

}
