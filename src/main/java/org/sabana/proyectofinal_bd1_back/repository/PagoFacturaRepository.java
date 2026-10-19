package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.PagoFactura;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para los pagos asociados a una factura (tabla pago).
 */
public interface PagoFacturaRepository {

    void saveBatch(List<PagoFactura> pagos);

    List<PagoFactura> findByNumFactura(String numFactura);

    List<PagoFactura> findAll();

    Optional<PagoFactura> findById(Long idPago);

}
