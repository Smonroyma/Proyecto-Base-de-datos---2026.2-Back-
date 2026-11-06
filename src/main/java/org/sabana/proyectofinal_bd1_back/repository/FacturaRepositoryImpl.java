package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.EstadoFactura;
import org.sabana.proyectofinal_bd1_back.model.entity.Factura;
import org.sabana.proyectofinal_bd1_back.model.entity.Pago;
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

@Repository
public class FacturaRepositoryImpl implements FacturaRepository {

    private final JdbcTemplate jdbcTemplate;
    private final PagoFacturaRepository pagoFacturaRepository;

    private static final String BASE_SELECT = """
            SELECT f.num_factura, f.pedido_id_pedido AS id_pedido,
                   f.fecha, f.estado, f.propina, f.impuesto, f.total,
                   p.fecha AS fecha_pedido, p.estado AS estado_pedido, p.observaciones AS observaciones_pedido,
                   c.id_cliente, c.nombre AS nombre_cliente, c.apellido AS apellido_cliente,
                   (SELECT cc.correo FROM cliente_correo cc WHERE cc.cliente_id_cliente = c.id_cliente ORDER BY cc.correo LIMIT 1) AS correo_cliente,
                   (SELECT ct.telefono FROM cliente_telefono ct WHERE ct.cliente_id_cliente = c.id_cliente ORDER BY ct.telefono LIMIT 1) AS telefono_cliente,
                   s.id_usuario, s.nombres AS nombres_usuario, s.apellidos AS apellidos_usuario,
                   s.correo AS correo_usuario
            FROM factura f
            INNER JOIN pedido p ON f.pedido_id_pedido = p.id_pedido
            INNER JOIN cliente c ON p.cliente_id_cliente = c.id_cliente
            INNER JOIN staff s ON p.staff_id_usuario = s.id_usuario
            """;

    private final RowMapper<Factura> facturaRowMapper = (rs, rowNum) -> {
        Long idPedido = rs.getObject("id_pedido") != null ? rs.getLong("id_pedido") : null;

        String nombre = rs.getString("nombre_cliente");
        String apellido = rs.getString("apellido_cliente");
        String nombreCliente = ((nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "")).trim();

        String nombresUsuario = rs.getString("nombres_usuario");
        String apellidosUsuario = rs.getString("apellidos_usuario");
        String nombreUsuario = ((nombresUsuario != null ? nombresUsuario : "") + " " + (apellidosUsuario != null ? apellidosUsuario : "")).trim();

        Timestamp fecha = rs.getTimestamp("fecha");
        return Factura.builder()
                .numFactura(rs.getString("num_factura"))
                .idPedido(idPedido)
                .nombreCliente(nombreCliente.isBlank() ? null : nombreCliente)
                .correoCliente(rs.getString("correo_cliente"))
                .telefonoCliente(rs.getString("telefono_cliente"))
                .nombreUsuario(nombreUsuario.isBlank() ? null : nombreUsuario)
                .fecha(fecha != null ? fecha.toLocalDateTime() : null)
                .estado(rs.getString("estado"))
                .propina(rs.getObject("propina") != null ? rs.getDouble("propina") : 0.0)
                .impuesto(rs.getObject("impuesto") != null ? rs.getDouble("impuesto") : 0.0)
                .total(rs.getObject("total") != null ? rs.getDouble("total") : 0.0)
                .pagos(new ArrayList<>())
                .build();
    };

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
                List<Pago> pagos = pagoFacturaRepository.findByNumFactura(factura.getNumFactura());
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
        String sql = BASE_SELECT + " WHERE f.pedido_id_pedido = ? LIMIT 1";
        try {
            Factura factura = jdbcTemplate.queryForObject(sql, facturaRowMapper, idPedido);
            if (factura != null) {
                List<Pago> pagos = pagoFacturaRepository.findByNumFactura(factura.getNumFactura());
                factura.setPagos(pagos);
            }
            return Optional.ofNullable(factura);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Factura> findByClienteId(Long idCliente) {
        String sql = BASE_SELECT + " WHERE p.cliente_id_cliente = ? ORDER BY f.fecha DESC, f.num_factura DESC";
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
                INSERT INTO factura (num_factura, pedido_id_pedido, fecha, estado, propina, impuesto, total)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        LocalDateTime fecha = factura.getFecha() != null ? factura.getFecha() : LocalDateTime.now();
        String estadoUpper = factura.getEstado() != null
                ? factura.getEstado().trim().toUpperCase()
                : EstadoFactura.PAGADA.name();

        jdbcTemplate.update(
                sql,
                factura.getNumFactura(),
                factura.getIdPedido(),
                Timestamp.valueOf(fecha),
                estadoUpper,
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
        String sql = "SELECT COUNT(*) FROM factura WHERE pedido_id_pedido = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idPedido);
        return count != null && count > 0;
    }

    private void cargarPagosEnLista(List<Factura> facturas) {
        for (Factura f : facturas) {
            List<Pago> pagos = pagoFacturaRepository.findByNumFactura(f.getNumFactura());
            f.setPagos(pagos);
        }
    }

}
