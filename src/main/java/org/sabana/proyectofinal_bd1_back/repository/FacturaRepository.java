package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Factura;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para la entidad Factura.
 */
public interface FacturaRepository {

    Optional<Factura> findByNumFactura(String numFactura);

    List<Factura> findAll();

    Optional<Factura> findByPedidoId(Long idPedido);

    List<Factura> findByClienteId(Long idCliente);

    List<Factura> findByRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    Factura save(Factura factura);

    String generarSiguienteNumeroFactura();

    boolean existsByPedidoId(Long idPedido);

}
