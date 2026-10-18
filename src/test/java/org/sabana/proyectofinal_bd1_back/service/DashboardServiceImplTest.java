package org.sabana.proyectofinal_bd1_back.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sabana.proyectofinal_bd1_back.model.dto.DashboardResumenDTO;
import org.sabana.proyectofinal_bd1_back.repository.DashboardRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("obtenerResumenOperativo consolida y retorna todas las métricas cuantitativas pobladas")
    void obtenerResumenOperativo_ConMetricasPobladas_Exitoso() {
        when(dashboardRepository.countProductosActivos()).thenReturn(28);
        when(dashboardRepository.countMesasDisponibles()).thenReturn(10);
        when(dashboardRepository.countPedidosActivos()).thenReturn(6);
        when(dashboardRepository.countPedidosEntregadosHoy(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(45);
        when(dashboardRepository.sumTotalVentasHoy(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(1485000.0);
        when(dashboardRepository.countReservasHoy(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(8);

        DashboardResumenDTO resumen = dashboardService.obtenerResumenOperativo();

        assertNotNull(resumen);
        assertEquals(28, resumen.getProductosActivos());
        assertEquals(10, resumen.getMesasDisponibles());
        assertEquals(6, resumen.getPedidosActivos());
        assertEquals(45, resumen.getPedidosEntregadosHoy());
        assertEquals(1485000.0, resumen.getTotalVentasHoy());
        assertEquals(8, resumen.getReservasHoy());

        verify(dashboardRepository).countProductosActivos();
        verify(dashboardRepository).countMesasDisponibles();
        verify(dashboardRepository).countPedidosActivos();
        verify(dashboardRepository).countPedidosEntregadosHoy(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(dashboardRepository).sumTotalVentasHoy(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(dashboardRepository).countReservasHoy(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("obtenerResumenOperativo maneja métricas en cero al iniciar la jornada sin ventas ni pedidos")
    void obtenerResumenOperativo_InicioDeJornadaEnCero_Exitoso() {
        when(dashboardRepository.countProductosActivos()).thenReturn(15);
        when(dashboardRepository.countMesasDisponibles()).thenReturn(12);
        when(dashboardRepository.countPedidosActivos()).thenReturn(0);
        when(dashboardRepository.countPedidosEntregadosHoy(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0);
        when(dashboardRepository.sumTotalVentasHoy(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0.0);
        when(dashboardRepository.countReservasHoy(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0);

        DashboardResumenDTO resumen = dashboardService.obtenerResumenOperativo();

        assertNotNull(resumen);
        assertEquals(15, resumen.getProductosActivos());
        assertEquals(12, resumen.getMesasDisponibles());
        assertEquals(0, resumen.getPedidosActivos());
        assertEquals(0, resumen.getPedidosEntregadosHoy());
        assertEquals(0.0, resumen.getTotalVentasHoy());
        assertEquals(0, resumen.getReservasHoy());
    }

    @Test
    @DisplayName("obtenerResumenOperativo asigna valores por defecto seguros (0 y 0.0) si el repositorio retorna null")
    void obtenerResumenOperativo_ValoresNulosDelRepositorio_AsignaValoresPorDefecto() {
        when(dashboardRepository.countProductosActivos()).thenReturn(null);
        when(dashboardRepository.countMesasDisponibles()).thenReturn(null);
        when(dashboardRepository.countPedidosActivos()).thenReturn(null);
        when(dashboardRepository.countPedidosEntregadosHoy(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);
        when(dashboardRepository.sumTotalVentasHoy(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);
        when(dashboardRepository.countReservasHoy(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);

        DashboardResumenDTO resumen = dashboardService.obtenerResumenOperativo();

        assertNotNull(resumen);
        assertEquals(0, resumen.getProductosActivos());
        assertEquals(0, resumen.getMesasDisponibles());
        assertEquals(0, resumen.getPedidosActivos());
        assertEquals(0, resumen.getPedidosEntregadosHoy());
        assertEquals(0.0, resumen.getTotalVentasHoy());
        assertEquals(0, resumen.getReservasHoy());
    }

    @Test
    @DisplayName("obtenerResumenOperativo envía exactamente el rango 00:00:00 a 23:59:59 del día actual en zona America/Bogota")
    void obtenerResumenOperativo_VerificaRangoFechasInicioYFinDeJornada() {
        LocalDate hoyEsperado = LocalDate.now(DashboardServiceImpl.ZONA_HORARIA_BOGOTA);

        when(dashboardRepository.countProductosActivos()).thenReturn(10);
        when(dashboardRepository.countMesasDisponibles()).thenReturn(5);
        when(dashboardRepository.countPedidosActivos()).thenReturn(2);
        when(dashboardRepository.countPedidosEntregadosHoy(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10);
        when(dashboardRepository.sumTotalVentasHoy(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(250000.0);
        when(dashboardRepository.countReservasHoy(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3);

        dashboardService.obtenerResumenOperativo();

        ArgumentCaptor<LocalDateTime> inicioCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> finCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(dashboardRepository).countPedidosEntregadosHoy(inicioCaptor.capture(), finCaptor.capture());
        verify(dashboardRepository).sumTotalVentasHoy(inicioCaptor.getValue(), finCaptor.getValue());
        verify(dashboardRepository).countReservasHoy(inicioCaptor.getValue(), finCaptor.getValue());

        LocalDateTime inicioEnviado = inicioCaptor.getValue();
        LocalDateTime finEnviado = finCaptor.getValue();

        assertEquals(LocalTime.MIDNIGHT, inicioEnviado.toLocalTime());
        assertEquals(LocalTime.of(23, 59, 59), finEnviado.toLocalTime());
        assertEquals(inicioEnviado.toLocalDate(), finEnviado.toLocalDate());
        assertTrue(!inicioEnviado.toLocalDate().isBefore(hoyEsperado));
    }

}
