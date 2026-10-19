package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO de entrada para procesar el pago de un pedido (POST /api/pagos) y generar automáticamente su factura con 19% IVA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de registro de pago de un pedido y emisión automática de factura con IVA 19%")
public class FacturaRequestDTO {

    @NotNull(message = "El ID del pedido a pagar es obligatorio")
    @Schema(description = "Identificador del pedido que se va a pagar y facturar", example = "1")
    private Long idPedido;

    @Schema(description = "Identificador del empleado que registra el pago (opcional, si no se envía se toma del pedido)", example = "2")
    private Long idUsuario;

    @Min(value = 0, message = "La propina no puede ser negativa")
    @Schema(description = "Monto de propina voluntaria (opcional, por defecto 0.0)", example = "2000.0")
    private Double propina;

    @Valid
    @Schema(description = "Lista de pagos realizados por método de pago (EFECTIVO, TARJETA, TRANSFERENCIA)")
    private List<PagoRequestDTO> pagos;

    @JsonAlias({"tipo", "metodoPago"})
    @Schema(description = "Tipo de método de pago directo (alternativa si se realiza un único pago)", example = "EFECTIVO")
    private String tipoMetodoPago;

    @JsonAlias({"monto"})
    @Schema(description = "Total abonado directo (alternativa si se realiza un único pago)", example = "22795.0")
    private Double total;

    @Schema(description = "Referencia de transacción directa (opcional)", example = "VOUCHER-98412")
    private String referenciaTransaccion;

    public List<PagoRequestDTO> resolvePagos() {
        if (pagos != null && !pagos.isEmpty()) {
            return pagos;
        }
        if (tipoMetodoPago != null && !tipoMetodoPago.isBlank() && total != null) {
            List<PagoRequestDTO> single = new ArrayList<>();
            single.add(PagoRequestDTO.builder()
                    .tipoMetodoPago(tipoMetodoPago)
                    .total(total)
                    .referenciaTransaccion(referenciaTransaccion)
                    .build());
            return single;
        }
        return pagos;
    }

}
