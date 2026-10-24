package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Pago;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para los pagos asociados a una factura (tabla pago).
 */
public interface PagoFacturaRepository {

    void saveBatch(List<Pago> pagos);

    List<Pago> findByNumFactura(String numFactura);

    List<Pago> findAll();

    Optional<Pago> findById(Long idPago);

}
