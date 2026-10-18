package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.PagoFactura;

import java.util.List;

/**
 * Contrato de persistencia para los pagos asociados a una factura (PagoFactura).
 */
public interface PagoFacturaRepository {

    void saveBatch(List<PagoFactura> pagos);

    List<PagoFactura> findByFacturaId(Long idFactura);

}
