package org.sabana.proyectofinal_bd1_back.repository;

import java.time.LocalDateTime;

/**
 * Contrato de persistencia para consultas de agregación y métricas del Dashboard Operativo.
 */
public interface DashboardRepository {

    Integer countProductosActivos();

    Integer countMesasDisponibles();

    Integer countPedidosActivos();

    Integer countPedidosEntregadosHoy(LocalDateTime inicioDia, LocalDateTime finDia);

    Double sumTotalVentasHoy(LocalDateTime inicioDia, LocalDateTime finDia);

    Integer countReservasHoy(LocalDateTime inicioDia, LocalDateTime finDia);

}
