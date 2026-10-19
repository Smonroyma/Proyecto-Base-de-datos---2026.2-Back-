package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.EstadoFactura;
import org.sabana.proyectofinal_bd1_back.model.entity.Factura;
import org.sabana.proyectofinal_bd1_back.model.entity.PagoFactura;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de persistencia para Factura mediante SQL nativo puro y JdbcTemplate.
 */
@Repository
public class FacturaRepositoryImpl implements FacturaRepository {

    private final JdbcTemplate jdbcTemplate;
    private final PagoFacturaRepository pagoFacturaRepository;

    private static final String BASE_SELECT = """
            SELECT f.num_factura, f.id_pedido,
                   f.id_cliente,
                   CONCAT(c.nombres, ' ', c.apellidos) AS nombre_cliente,
                   c.correo AS correo_cliente,
                   c.telefono AS telefono_cliente,
                   f.id_usuario,
                   CONCAT(s.nombres, ' ', s.apellidos) AS nombre_usuario,
                   f.fecha, f.estado, f.subtotal, f.propina, f.impuesto, f.total
            FROM factura f
            INNER JOIN pedido p ON f.id_pedido = p.id_pedido
            INNER JOIN cliente c ON f.id_cliente = c.id_cliente
            INNER JOIN staff s ON f.id_usuario = s.id_usuario
            """;

    private final RowMapper<Factura> facturaRowMapper = (rs, rowNum) -> Factura.builder()
            .numFactura(rs.getString("num_factura"))
            .idPedido(rs.getObject("id_pedido") != null ? rs.getLong("id_pedido") : null)
            .idCliente(rs.getObject("id_cliente") != null ? rs.getLong("id_cliente") : null)
            .nombreCliente(rs.getString("nombre_cliente") != null ? rs.getString("nombre_cliente").trim() : null)
            .correoCliente(rs.getString("correo_cliente"))
            .telefonoCliente(rs.getString("telefono_cliente"))
            .idUsuario(rs.getObject("id_usuario") != null ? rs.getLong("id_usuario") : null)
            .nombreUsuario(rs.getString("nombre_usuario") != null ? rs.getString("nombre_usuario").trim() : null)
            .fecha(rs.getTimestamp("fecha") != null
                    ? rs.getTimestamp("fecha").toLocalDateTime()
                    : null)
            .estado(rs.getString("estado"))
            .subtotal(rs.getObject("subtotal") != null ? rs.getDouble("subtotal") : 0.0)
            .propina(rs.getObject("propina") != null ? rs.getDouble("propina") : 0.0)
            .impuesto(rs.getObject("impuesto") != null ? rs.getDouble("impuesto") : 0.0)
            .total(rs.getObject("total") != null ? rs.getDouble("total") : 0.0)
            .pagos(new ArrayList<>())
            .build();

    public FacturaRepositoryImpl(JdbcTemplate jdbcTemplate, PagoFacturaRepository pagoFacturaRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.pagoFacturaRepository = pagoFacturaRepository;
    }

    @Override
    public Optional<Factura> findByNumFactura(String numFactura) {
        String sql = BASE_SELECT + " WHERE UPPER(TRIM(f.num_factura)) = UPPER(TRIM(?))";
        try {
            Factura factura = jdbcTemplate.queryForObject(sql, facturaRowMapper, numFactura);
            if (factura != null) {
                List<PagoFactura> pagos = pagoFacturaRepository.findByNumFactura(factura.getNumFactura());
                factura.setPagos(pagos);
            }
            return Optional.ofNullable(factura);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Factura> findAll() {
        String sql = BASE_SELECT + " ORDER BY f.fecha DESC, f.num_factura DESC";
        List<Factura> facturas = jdbcTemplate.query(sql, facturaRowMapper);
        cargarPagosEnLista(facturas);
        return facturas;
    }

    @Override
    public Optional<Factura> findByPedidoId(Long idPedido) {
        String sql = BASE_SELECT + " WHERE f.id_pedido = ? LIMIT 1";
        try {
            Factura factura = jdbcTemplate.queryForObject(sql, facturaRowMapper, idPedido);
            if (factura != null) {
                List<PagoFactura> pagos = pagoFacturaRepository.findByNumFactura(factura.getNumFactura());
                factura.setPagos(pagos);
            }
            return Optional.ofNullable(factura);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Factura> findByClienteId(Long idCliente) {
        String sql = BASE_SELECT + " WHERE f.id_cliente = ? ORDER BY f.fecha DESC, f.num_factura DESC";
        List<Factura> facturas = jdbcTemplate.query(sql, facturaRowMapper, idCliente);
        cargarPagosEnLista(facturas);
        return facturas;
    }

    @Override
    public List<Factura> findByRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        String sql = BASE_SELECT + " WHERE f.fecha BETWEEN ? AND ? ORDER BY f.fecha DESC, f.num_factura DESC";
        List<Factura> facturas = jdbcTemplate.query(
                sql,
                facturaRowMapper,
                Timestamp.valueOf(fechaInicio),
                Timestamp.valueOf(fechaFin)
        );
        cargarPagosEnLista(facturas);
        return facturas;
    }

    @Override
    public Factura save(Factura factura) {
        String sql = """
                INSERT INTO factura (num_factura, id_pedido, id_cliente, id_usuario, fecha,
                                     estado, subtotal, propina, impuesto, total)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        LocalDateTime fecha = factura.getFecha() != null ? factura.getFecha() : LocalDateTime.now();
        String estadoUpper = factura.getEstado() != null
                ? factura.getEstado().trim().toUpperCase()
                : EstadoFactura.PAGADA.name();

        jdbcTemplate.update(
                sql,
                factura.getNumFactura(),
                factura.getIdPedido(),
                factura.getIdCliente(),
                factura.getIdUsuario(),
                Timestamp.valueOf(fecha),
                estadoUpper,
                factura.getSubtotal() != null ? factura.getSubtotal() : 0.0,
                factura.getPropina() != null ? factura.getPropina() : 0.0,
                factura.getImpuesto() != null ? factura.getImpuesto() : 0.0,
                factura.getTotal() != null ? factura.getTotal() : 0.0
        );

        factura.setFecha(fecha);
        factura.setEstado(estadoUpper);
        return factura;
    }

    @Override
    public String generarSiguienteNumeroFactura() {
        String sql = "SELECT COUNT(*) + 1 FROM factura";
        Long siguienteConsecutivo = jdbcTemplate.queryForObject(sql, Long.class);
        long consecutivo = siguienteConsecutivo != null ? siguienteConsecutivo : 1L;
        int anioActual = LocalDate.now().getYear();
        return String.format("FAC-%d-%05d", anioActual, consecutivo);
    }

    @Override
    public boolean existsByPedidoId(Long idPedido) {
        String sql = "SELECT COUNT(*) FROM factura WHERE id_pedido = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idPedido);
        return count != null && count > 0;
    }

    private void cargarPagosEnLista(List<Factura> facturas) {
        for (Factura f : facturas) {
            List<PagoFactura> pagos = pagoFacturaRepository.findByNumFactura(f.getNumFactura());
            f.setPagos(pagos);
        }
    }

}
