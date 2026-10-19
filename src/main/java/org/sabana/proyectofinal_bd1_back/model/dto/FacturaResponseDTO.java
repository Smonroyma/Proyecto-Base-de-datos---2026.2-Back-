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
 * DTO de salida con el comprobante completo de una factura (IVA 19%) y el desglose de sus pagos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta detallada de una factura emitida y sus pagos asociados")
public class FacturaResponseDTO {

    @Schema(description = "Número consecutivo fiscal único de la factura (PK)", example = "FAC-2026-00001")
    private String numFactura;

    @Schema(description = "Identificador del pedido facturado", example = "1")
    private Long idPedido;

    @Schema(description = "Identificador del cliente", example = "1")
    private Long idCliente;

    @Schema(description = "Nombre completo del cliente resuelto vía JOIN", example = "Carlos Mendoza")
    private String nombreCliente;

    @Schema(description = "Correo electrónico del cliente", example = "carlos.mendoza@email.com")
    private String correoCliente;

    @Schema(description = "Teléfono del cliente", example = "+57 300 1234567")
    private String telefonoCliente;

    @Schema(description = "Identificador del empleado que procesó el pago", example = "2")
    private Long idUsuario;

    @Schema(description = "Nombre del empleado que procesó el pago", example = "Laura Cajera")
    private String nombreUsuario;

    @Schema(description = "Fecha y hora de emisión de la factura", example = "2026-10-17T18:00:00")
    private LocalDateTime fecha;

    @Schema(description = "Estado actual de la factura", example = "PAGADA", allowableValues = {"EMITIDA", "PAGADA"})
    private String estado;

    @Schema(description = "Subtotal base tomado del pedido", example = "17500.0")
    private Double subtotal;

    @Schema(description = "Propina voluntaria incluida", example = "2000.0")
    private Double propina;

    @Schema(description = "Impuesto calculado al 19% (IVA)", example = "3325.0")
    private Double impuesto;

    @Schema(description = "Total final pagado (subtotal + impuesto 19% + propina)", example = "22825.0")
    private Double total;

    @Builder.Default
    @Schema(description = "Lista de pagos registrados para esta factura")
    private List<PagoResponseDTO> pagos = new ArrayList<>();

}
