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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de registro de pago de un pedido y emisión automática de factura con IVA 19%")
public class FacturaRequestDTO {

    @NotNull(message = "El ID del pedido a pagar es obligatorio")
    @Schema(description = "Identificador del pedido que se va a pagar y facturar", example = "1")
    private Long idPedido;

    @Min(value = 0, message = "La propina no puede ser negativa")
    @Schema(description = "Monto de propina voluntaria (opcional, por defecto 0.0)", example = "2000.0")
    private Double propina;

    @Valid
    @Schema(description = "Lista de pagos realizados por método de pago (EFECTIVO, TARJETA, TRANSFERENCIA)")
    private List<PagoRequestDTO> pagos;

    @JsonAlias({"tipo", "metodoPago"})
    @Schema(description = "Tipo de método de pago directo (alternativa si se realiza un único pago)", example = "EFECTIVO")
    private String tipoMetodoPago;

    @JsonAlias({"total"})
    @Schema(description = "Monto abonado directo (alternativa si se realiza un único pago)", example = "22795.0")
    private Double monto;

    public List<PagoRequestDTO> resolvePagos() {
        if (pagos != null && !pagos.isEmpty()) {
            return pagos;
        }
        if (tipoMetodoPago != null && !tipoMetodoPago.isBlank() && monto != null) {
            List<PagoRequestDTO> single = new ArrayList<>();
            single.add(PagoRequestDTO.builder()
                    .tipoMetodoPago(tipoMetodoPago)
                    .monto(monto)
                    .build());
            return single;
        }
        return pagos;
    }

}
