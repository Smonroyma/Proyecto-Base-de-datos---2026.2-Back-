package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad de dominio PagoFactura.
 * Mapea directamente la tabla de base de datos 'pago_factura' sin anotaciones JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoFactura {

    private Long idPago;
    private Long idFactura;
    private Long idMetodoPago;
    private String nombreMetodoPago;
    private Double monto;
    private String referenciaTransaccion;
    private LocalDateTime fechaHora;

}
