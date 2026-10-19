package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Pedido;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para Pedido mediante SQL nativo.
 */
public interface PedidoRepository {

    Optional<Pedido> findById(Long id);

    List<Pedido> findAll();

    List<Pedido> findByCliente(Long idCliente);

    List<Pedido> findByMesa(Integer numeroMesa);

    List<Pedido> findByUsuario(Long idUsuario);

    List<Pedido> findByEstado(String estado);

    Pedido save(Pedido pedido);

    boolean updateEstado(Long idPedido, String nuevoEstado);

    boolean deleteById(Long id);

}
