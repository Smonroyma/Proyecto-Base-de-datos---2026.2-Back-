package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad de dominio Pago.
 * POJO puro sin anotaciones de persistencia JPA.
 * Regla V3: Usa 'monto' y no contiene 'referenciaTransaccion'.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    private Long idPago;
    private LocalDateTime fecha;
    private String estado;
    private Double monto;
    private String numFactura;
    private String tipoMetodoPago;

}
