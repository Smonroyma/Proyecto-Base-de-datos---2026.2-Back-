package org.sabana.proyectofinal_bd1_back.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Implementación de DashboardRepository mediante consultas de agregación en SQL nativo puro y JdbcTemplate.
 */
@Repository
public class DashboardRepositoryImpl implements DashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    public DashboardRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Integer countProductosActivos() {
        String sql = "SELECT COUNT(*) FROM producto WHERE disponibilidad = true";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public Integer countMesasDisponibles() {
        String sql = "SELECT COUNT(*) FROM mesa WHERE estado = 'DISPONIBLE'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public Integer countPedidosActivos() {
        String sql = "SELECT COUNT(*) FROM pedido WHERE estado IN ('PENDIENTE', 'EN_PREPARACION')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public Integer countPedidosEntregadosHoy(LocalDateTime inicioDia, LocalDateTime finDia) {
        String sql = """
                SELECT COUNT(*) FROM pedido
                WHERE estado IN ('ENTREGADO', 'PAGADO')
                  AND fecha >= ? AND fecha <= ?
                """;
        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                Timestamp.valueOf(inicioDia),
                Timestamp.valueOf(finDia)
        );
        return count != null ? count : 0;
    }

    @Override
    public Double sumTotalVentasHoy(LocalDateTime inicioDia, LocalDateTime finDia) {
        String sql = """
                SELECT COALESCE(SUM(total), 0.0) FROM factura
                WHERE estado = 'PAGADA'
                  AND fecha >= ? AND fecha <= ?
                """;
        Double sum = jdbcTemplate.queryForObject(
                sql,
                Double.class,
                Timestamp.valueOf(inicioDia),
                Timestamp.valueOf(finDia)
        );
        return sum != null ? sum : 0.0;
    }

    @Override
    public Integer countReservasHoy(LocalDateTime inicioDia, LocalDateTime finDia) {
        String sql = """
                SELECT COUNT(*) FROM reserva
                WHERE estado IN ('PENDIENTE', 'CONFIRMADA')
                  AND fecha = ?
                """;
        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                Date.valueOf(inicioDia.toLocalDate())
        );
        return count != null ? count : 0;
    }

}
