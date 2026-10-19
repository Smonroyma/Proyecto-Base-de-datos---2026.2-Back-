package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de salida con la información de un pago aplicado a una factura.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle de un pago procesado en una factura")
public class PagoResponseDTO {

    @Schema(description = "Identificador único del registro de pago", example = "10")
    private Long idPago;

    @Schema(description = "Número de la factura asociada", example = "FAC-2026-00001")
    private String numFactura;

    @Schema(description = "Tipo del método de pago utilizado", example = "EFECTIVO")
    private String tipoMetodoPago;

    @Schema(description = "Nombre del método de pago resuelto vía JOIN", example = "Efectivo")
    private String nombreMetodoPago;

    @Schema(description = "Estado del pago", example = "PAGADO")
    private String estado;

    @Schema(description = "Monto total abonado en esta transacción", example = "15000.0")
    private Double total;

    @Schema(description = "Código de referencia o autorización de la transacción", example = "VOUCHER-98412")
    private String referenciaTransaccion;

    @Schema(description = "Fecha y hora exacta en que se registró el pago", example = "2026-10-17T18:00:00")
    private LocalDateTime fecha;

}
