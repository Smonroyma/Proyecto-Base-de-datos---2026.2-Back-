package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.DashboardResumenDTO;

/**
 * Contrato de servicio para la consolidación de métricas del Dashboard Operativo en tiempo real.
 */
public interface DashboardService {

    DashboardResumenDTO obtenerResumenOperativo();

}
