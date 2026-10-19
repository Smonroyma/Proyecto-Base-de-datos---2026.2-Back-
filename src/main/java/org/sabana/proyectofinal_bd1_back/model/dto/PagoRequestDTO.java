package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para un ítem de pago individual asociado a una factura.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de un pago individual seleccionando el tipo de METODO_PAGO")
public class PagoRequestDTO {

    @NotBlank(message = "El tipo de método de pago es obligatorio (EFECTIVO, TARJETA, TRANSFERENCIA)")
    @JsonAlias({"tipo", "metodoPago", "codigo", "idMetodoPago"})
    @Schema(description = "Tipo del método de pago utilizado (PK de metodo_pago)", example = "EFECTIVO", allowableValues = {"EFECTIVO", "TARJETA", "TRANSFERENCIA"})
    private String tipoMetodoPago;

    @NotNull(message = "El monto total del pago es obligatorio")
    @Positive(message = "El monto del pago debe ser mayor que cero")
    @JsonAlias({"monto"})
    @Schema(description = "Monto abonado con este método de pago", example = "15000.0")
    private Double total;

    @Size(max = 100, message = "La referencia de transacción no puede superar los 100 caracteres")
    @Schema(description = "Número de voucher, autorización o referencia de transferencia (opcional)", example = "VOUCHER-98412")
    private String referenciaTransaccion;

}
