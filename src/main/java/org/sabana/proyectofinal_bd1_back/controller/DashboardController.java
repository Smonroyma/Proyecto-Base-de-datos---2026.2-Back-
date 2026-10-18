package org.sabana.proyectofinal_bd1_back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sabana.proyectofinal_bd1_back.model.dto.DashboardResumenDTO;
import org.sabana.proyectofinal_bd1_back.service.DashboardService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ErrorResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la consulta del panel de control y analítica operativa en tiempo real.
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para la visualización de indicadores operativos y financieros en tiempo real")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumen")
    @Operation(
            summary = "Resumen operativo en tiempo real",
            description = "Consolida y retorna las métricas operativas del restaurante para la jornada actual (productos disponibles, mesas libres, pedidos en curso, pedidos entregados hoy, ventas facturadas hoy y reservas programadas hoy)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumen operativo obtenido exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DashboardResumenDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<DashboardResumenDTO> obtenerResumen() {
        DashboardResumenDTO resumen = dashboardService.obtenerResumenOperativo();
        return ResponseEntity.ok(resumen);
    }

}
