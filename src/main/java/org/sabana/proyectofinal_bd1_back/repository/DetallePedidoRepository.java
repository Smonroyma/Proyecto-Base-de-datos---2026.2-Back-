package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.DetallePedido;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para DetallePedido mediante SQL nativo y JdbcTemplate.
 * Clave compuesta (id_pedido, id_producto) sin clases intermedias JPA.
 */
public interface DetallePedidoRepository {

    void saveBatch(List<DetallePedido> detalles);

    List<DetallePedido> findByPedidoId(Long idPedido);

    default List<DetallePedido> findByIdPedido(Long idPedido) {
        return findByPedidoId(idPedido);
    }

    Optional<DetallePedido> findByIdPedidoAndIdProducto(Long idPedido, Long idProducto);

    boolean updateCantidad(Long idPedido, Long idProducto, int cantidad);

    boolean deleteByIdPedidoAndIdProducto(Long idPedido, Long idProducto);

    boolean deleteByPedidoId(Long idPedido);

    default boolean deleteByIdPedido(Long idPedido) {
        return deleteByPedidoId(idPedido);
    }

}
