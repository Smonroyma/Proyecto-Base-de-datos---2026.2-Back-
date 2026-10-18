package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de salida con el comprobante completo de una factura y el desglose de sus pagos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta detallada de una factura emitida y sus pagos asociados")
public class FacturaResponseDTO {

    @Schema(description = "Identificador único de la factura", example = "100")
    private Long idFactura;

    @Schema(description = "Número consecutivo fiscal de la factura", example = "FAC-2026-00001")
    private String numeroFactura;

    @Schema(description = "Identificador del pedido facturado", example = "50")
    private Long idPedido;

    @Schema(description = "Identificador del cliente", example = "1")
    private Long idCliente;

    @Schema(description = "Nombre completo del cliente resuelto vía JOIN", example = "Carlos Mendoza")
    private String nombreCliente;

    @Schema(description = "Identificación o contacto del cliente resuelto vía JOIN", example = "3001234567")
    private String identificacionCliente;

    @Schema(description = "Identificador del usuario/cajero que emitió la factura", example = "2")
    private Long idUsuario;

    @Schema(description = "Nombre del cajero que emitió la factura resuelto vía JOIN", example = "Laura Cajera")
    private String nombreUsuario;

    @Schema(description = "Fecha y hora de emisión de la factura", example = "2026-10-17T18:00:00")
    private LocalDateTime fechaEmision;

    @Schema(description = "Subtotal base tomado del pedido", example = "25000.0")
    private Double subtotal;

    @Schema(description = "Propina voluntaria incluida", example = "2000.0")
    private Double propina;

    @Schema(description = "Descuento aplicado a la factura", example = "0.0")
    private Double descuento;

    @Schema(description = "Impuesto calculado (IVA / Impoconsumo)", example = "2000.0")
    private Double impuesto;

    @Schema(description = "Total final pagado (subtotal + impuesto + propina - descuento)", example = "29000.0")
    private Double total;

    @Schema(description = "Estado actual de la factura", example = "PAGADA", allowableValues = {"EMITIDA", "PAGADA", "ANULADA"})
    private String estado;

    @Builder.Default
    @Schema(description = "Lista de pagos registrados para esta factura")
    private List<PagoResponseDTO> pagos = new ArrayList<>();

}
