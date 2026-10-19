package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad de dominio PagoFactura (mapea la tabla 'pago' del modelo V2).
 * Sin anotaciones JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoFactura {

    private Long idPago;
    private String numFactura;
    private String tipoMetodoPago;
    private String nombreMetodoPago;
    private LocalDateTime fecha;
    private String estado;
    private Double total;
    private String referenciaTransaccion;

}
