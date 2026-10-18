package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.DashboardResumenDTO;
import org.sabana.proyectofinal_bd1_back.repository.DashboardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Implementación de la lógica de negocio para el Dashboard Operativo y Analítica en Tiempo Real.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    public static final ZoneId ZONA_HORARIA_BOGOTA = ZoneId.of("America/Bogota");

    private final DashboardRepository dashboardRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResumenDTO obtenerResumenOperativo() {
        LocalDate hoy = LocalDate.now(ZONA_HORARIA_BOGOTA);
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(LocalTime.of(23, 59, 59));

        Integer productosActivos = dashboardRepository.countProductosActivos();
        Integer mesasDisponibles = dashboardRepository.countMesasDisponibles();
        Integer pedidosActivos = dashboardRepository.countPedidosActivos();
        Integer pedidosEntregadosHoy = dashboardRepository.countPedidosEntregadosHoy(inicioDia, finDia);
        Double totalVentasHoy = dashboardRepository.sumTotalVentasHoy(inicioDia, finDia);
        Integer reservasHoy = dashboardRepository.countReservasHoy(inicioDia, finDia);

        return DashboardResumenDTO.builder()
                .productosActivos(productosActivos != null ? productosActivos : 0)
                .mesasDisponibles(mesasDisponibles != null ? mesasDisponibles : 0)
                .pedidosActivos(pedidosActivos != null ? pedidosActivos : 0)
                .pedidosEntregadosHoy(pedidosEntregadosHoy != null ? pedidosEntregadosHoy : 0)
                .totalVentasHoy(totalVentasHoy != null ? totalVentasHoy : 0.0)
                .reservasHoy(reservasHoy != null ? reservasHoy : 0)
                .build();
    }

}
