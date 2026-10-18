package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de entrada para la emisión de una factura y el registro de sus pagos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de emisión de factura y procesamiento de pagos")
public class FacturaRequestDTO {

    @NotNull(message = "El ID del pedido a facturar es obligatorio")
    @Schema(description = "Identificador del pedido que se va a facturar", example = "50")
    private Long idPedido;

    @NotNull(message = "El ID del usuario (cajero) es obligatorio")
    @Schema(description = "Identificador del cajero o usuario que emite la factura", example = "2")
    private Long idUsuario;

    @Min(value = 0, message = "La propina no puede ser negativa")
    @Schema(description = "Monto de propina voluntaria (opcional, por defecto 0.0)", example = "2000.0")
    private Double propina;

    @Min(value = 0, message = "El descuento no puede ser negativo")
    @Schema(description = "Monto de descuento aplicado (opcional, por defecto 0.0)", example = "0.0")
    private Double descuento;

    @NotEmpty(message = "La factura debe incluir al menos un pago")
    @Valid
    @Schema(description = "Lista de pagos realizados para cubrir el total de la factura")
    private List<PagoRequestDTO> pagos;

}
