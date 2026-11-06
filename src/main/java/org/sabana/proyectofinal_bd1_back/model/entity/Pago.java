package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
