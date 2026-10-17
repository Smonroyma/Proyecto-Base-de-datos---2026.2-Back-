package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.DetallePedido;

import java.util.List;

/**
 * Contrato de persistencia para DetallePedido mediante SQL nativo.
 */
public interface DetallePedidoRepository {

    void saveBatch(List<DetallePedido> detalles);

    List<DetallePedido> findByPedidoId(Long idPedido);

    boolean deleteByPedidoId(Long idPedido);

}
