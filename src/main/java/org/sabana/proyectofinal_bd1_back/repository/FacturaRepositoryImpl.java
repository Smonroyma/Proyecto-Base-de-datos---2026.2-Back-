package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.EstadoFactura;
import org.sabana.proyectofinal_bd1_back.model.entity.Factura;
import org.sabana.proyectofinal_bd1_back.model.entity.PagoFactura;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
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
            SELECT f.id_factura, f.numero_factura, f.id_pedido,
                   f.id_cliente, c.nombre AS nombre_cliente, c.telefono AS identificacion_cliente,
                   f.id_usuario, u.nombre AS nombre_usuario,
                   f.fecha_emision, f.subtotal, f.propina, f.descuento, f.impuesto, f.total, f.estado
            FROM factura f
            LEFT JOIN pedido p ON f.id_pedido = p.id_pedido
            LEFT JOIN cliente c ON f.id_cliente = c.id_cliente
            LEFT JOIN usuario u ON f.id_usuario = u.id_usuario
            """;

    private final RowMapper<Factura> facturaRowMapper = (rs, rowNum) -> Factura.builder()
            .idFactura(rs.getLong("id_factura"))
            .numeroFactura(rs.getString("numero_factura"))
            .idPedido(rs.getObject("id_pedido") != null ? rs.getLong("id_pedido") : null)
            .idCliente(rs.getObject("id_cliente") != null ? rs.getLong("id_cliente") : null)
            .nombreCliente(rs.getString("nombre_cliente"))
            .identificacionCliente(rs.getString("identificacion_cliente"))
            .idUsuario(rs.getObject("id_usuario") != null ? rs.getLong("id_usuario") : null)
            .nombreUsuario(rs.getString("nombre_usuario"))
            .fechaEmision(rs.getTimestamp("fecha_emision") != null
                    ? rs.getTimestamp("fecha_emision").toLocalDateTime()
                    : null)
            .subtotal(rs.getObject("subtotal") != null ? rs.getDouble("subtotal") : 0.0)
            .propina(rs.getObject("propina") != null ? rs.getDouble("propina") : 0.0)
            .descuento(rs.getObject("descuento") != null ? rs.getDouble("descuento") : 0.0)
            .impuesto(rs.getObject("impuesto") != null ? rs.getDouble("impuesto") : 0.0)
            .total(rs.getObject("total") != null ? rs.getDouble("total") : 0.0)
            .estado(rs.getString("estado"))
            .pagos(new ArrayList<>())
            .build();

    public FacturaRepositoryImpl(JdbcTemplate jdbcTemplate, PagoFacturaRepository pagoFacturaRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.pagoFacturaRepository = pagoFacturaRepository;
    }

    @Override
    public Optional<Factura> findById(Long id) {
        String sql = BASE_SELECT + " WHERE f.id_factura = ?";
        try {
            Factura factura = jdbcTemplate.queryForObject(sql, facturaRowMapper, id);
            if (factura != null) {
                List<PagoFactura> pagos = pagoFacturaRepository.findByFacturaId(id);
                factura.setPagos(pagos);
            }
            return Optional.ofNullable(factura);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Factura> findAll() {
        String sql = BASE_SELECT + " ORDER BY f.fecha_emision DESC, f.id_factura DESC";
        List<Factura> facturas = jdbcTemplate.query(sql, facturaRowMapper);
        cargarPagosEnLista(facturas);
        return facturas;
    }

    @Override
    public Optional<Factura> findByPedidoId(Long idPedido) {
        String sql = BASE_SELECT + " WHERE f.id_pedido = ? ORDER BY f.id_factura DESC LIMIT 1";
        try {
            Factura factura = jdbcTemplate.queryForObject(sql, facturaRowMapper, idPedido);
            if (factura != null) {
                List<PagoFactura> pagos = pagoFacturaRepository.findByFacturaId(factura.getIdFactura());
                factura.setPagos(pagos);
            }
            return Optional.ofNullable(factura);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Factura> findByClienteId(Long idCliente) {
        String sql = BASE_SELECT + " WHERE f.id_cliente = ? ORDER BY f.fecha_emision DESC, f.id_factura DESC";
        List<Factura> facturas = jdbcTemplate.query(sql, facturaRowMapper, idCliente);
        cargarPagosEnLista(facturas);
        return facturas;
    }

    @Override
    public List<Factura> findByRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        String sql = BASE_SELECT + " WHERE f.fecha_emision BETWEEN ? AND ? ORDER BY f.fecha_emision DESC, f.id_factura DESC";
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
                INSERT INTO factura (numero_factura, id_pedido, id_cliente, id_usuario, fecha_emision,
                                     subtotal, propina, descuento, impuesto, total, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime fechaEmision = factura.getFechaEmision() != null ? factura.getFechaEmision() : LocalDateTime.now();
        String estadoUpper = factura.getEstado() != null
                ? factura.getEstado().trim().toUpperCase()
                : EstadoFactura.PAGADA.name();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id_factura" });
            ps.setString(1, factura.getNumeroFactura());
            ps.setLong(2, factura.getIdPedido());
            ps.setLong(3, factura.getIdCliente());
            ps.setLong(4, factura.getIdUsuario());
            ps.setTimestamp(5, Timestamp.valueOf(fechaEmision));
            ps.setDouble(6, factura.getSubtotal() != null ? factura.getSubtotal() : 0.0);
            ps.setDouble(7, factura.getPropina() != null ? factura.getPropina() : 0.0);
            ps.setDouble(8, factura.getDescuento() != null ? factura.getDescuento() : 0.0);
            ps.setDouble(9, factura.getImpuesto() != null ? factura.getImpuesto() : 0.0);
            ps.setDouble(10, factura.getTotal() != null ? factura.getTotal() : 0.0);
            ps.setString(11, estadoUpper);
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            factura.setIdFactura(generatedKey.longValue());
        }
        factura.setFechaEmision(fechaEmision);
        factura.setEstado(estadoUpper);
        return factura;
    }

    @Override
    public boolean updateEstado(Long idFactura, String nuevoEstado) {
        String sql = "UPDATE factura SET estado = ? WHERE id_factura = ?";
        int rowsAffected = jdbcTemplate.update(sql, nuevoEstado.trim().toUpperCase(), idFactura);
        return rowsAffected > 0;
    }

    @Override
    public String generarSiguienteNumeroFactura() {
        String sql = "SELECT COALESCE(MAX(id_factura), 0) + 1 FROM factura";
        Long siguienteConsecutivo = jdbcTemplate.queryForObject(sql, Long.class);
        long consecutivo = siguienteConsecutivo != null ? siguienteConsecutivo : 1L;
        int anioActual = LocalDate.now().getYear();
        return String.format("FAC-%d-%05d", anioActual, consecutivo);
    }

    @Override
    public boolean existsByPedidoId(Long idPedido) {
        String sql = "SELECT COUNT(*) FROM factura WHERE id_pedido = ? AND UPPER(estado) <> 'ANULADA'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idPedido);
        return count != null && count > 0;
    }

    private void cargarPagosEnLista(List<Factura> facturas) {
        for (Factura f : facturas) {
            List<PagoFactura> pagos = pagoFacturaRepository.findByFacturaId(f.getIdFactura());
            f.setPagos(pagos);
        }
    }

}
