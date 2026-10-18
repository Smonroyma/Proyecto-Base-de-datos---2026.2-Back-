package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para un pago individual asociado a una factura.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de un pago individual dentro del cobro de una factura")
public class PagoRequestDTO {

    @NotNull(message = "El ID del método de pago es obligatorio")
    @Schema(description = "Identificador del método de pago utilizado", example = "1")
    private Long idMetodoPago;

    @NotNull(message = "El monto del pago es obligatorio")
    @Positive(message = "El monto del pago debe ser mayor que cero")
    @Schema(description = "Monto abonado con este método de pago", example = "15000.0")
    private Double monto;

    @Size(max = 100, message = "La referencia de transacción no puede superar los 100 caracteres")
    @Schema(description = "Número de voucher, autorización o referencia de transferencia (opcional)", example = "VOUCHER-98412")
    private String referenciaTransaccion;

}
