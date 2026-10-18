package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida con el balance cuantitativo y operativo en tiempo real de Savana Data Coffee.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen cuantitativo y analítico en tiempo real del restaurante")
public class DashboardResumenDTO {

    @Schema(description = "Cantidad total de productos activos y disponibles en el catálogo", example = "24")
    private Integer productosActivos;

    @Schema(description = "Cantidad de mesas con estado DISPONIBLE actualmente", example = "8")
    private Integer mesasDisponibles;

    @Schema(description = "Cantidad de pedidos activos en curso (PENDIENTE o EN_PREPARACION)", example = "5")
    private Integer pedidosActivos;

    @Schema(description = "Cantidad de pedidos entregados o pagados durante la jornada actual", example = "42")
    private Integer pedidosEntregadosHoy;

    @Schema(description = "Suma total de ventas facturadas y pagadas en el día actual", example = "1250000.0")
    private Double totalVentasHoy;

    @Schema(description = "Total de reservas programadas para hoy en estado PENDIENTE o CONFIRMADA", example = "6")
    private Integer reservasHoy;

}
