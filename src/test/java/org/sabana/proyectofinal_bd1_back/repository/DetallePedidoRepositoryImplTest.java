package org.sabana.proyectofinal_bd1_back.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sabana.proyectofinal_bd1_back.model.entity.DetallePedido;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetallePedidoRepositoryImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DetallePedidoRepositoryImpl repository;

    private DetallePedido detalle;

    @BeforeEach
    void setUp() {
        detalle = DetallePedido.builder()
                .idPedido(50L)
                .idProducto(10L)
                .nombreProducto("Café Americano")
                .cantidad(2)
                .precio(6500.0)
                .build();
    }

    @Test
    @DisplayName("saveBatch ejecuta batchUpdate con la lista de detalles")
    void saveBatch_EjecutaBatchUpdate() {
        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1});

        repository.saveBatch(List.of(detalle));

        verify(jdbcTemplate).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    @DisplayName("saveBatch con lista vacía no invoca jdbcTemplate")
    void saveBatch_ListaVacia_NoHaceNada() {
        repository.saveBatch(List.of());
        repository.saveBatch(null);

        verify(jdbcTemplate, org.mockito.Mockito.never())
                .batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    @DisplayName("findByIdPedidoAndIdProducto retorna el detalle cuando existe")
    @SuppressWarnings("unchecked")
    void findByIdPedidoAndIdProducto_Existe_RetornaDetalle() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(50L), eq(10L)))
                .thenReturn(detalle);

        Optional<DetallePedido> result = repository.findByIdPedidoAndIdProducto(50L, 10L);

        assertTrue(result.isPresent());
        assertEquals(50L, result.get().getIdPedido());
        assertEquals(10L, result.get().getIdProducto());
        assertEquals(2, result.get().getCantidad());
    }

    @Test
    @DisplayName("findByIdPedidoAndIdProducto retorna empty cuando no existe")
    @SuppressWarnings("unchecked")
    void findByIdPedidoAndIdProducto_NoExiste_RetornaEmpty() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(50L), eq(99L)))
                .thenThrow(new EmptyResultDataAccessException(1));

        Optional<DetallePedido> result = repository.findByIdPedidoAndIdProducto(50L, 99L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByPedidoId retorna lista de detalles del pedido")
    @SuppressWarnings("unchecked")
    void findByPedidoId_RetornaLista() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(50L)))
                .thenReturn(List.of(detalle));

        List<DetallePedido> result = repository.findByPedidoId(50L);

        assertEquals(1, result.size());
        assertEquals("Café Americano", result.get(0).getNombreProducto());
    }

    @Test
    @DisplayName("updateCantidad actualiza correctamente la cantidad por clave compuesta")
    void updateCantidad_ActualizaExitosamente() {
        when(jdbcTemplate.update(anyString(), eq(5), eq(50L), eq(10L))).thenReturn(1);

        boolean updated = repository.updateCantidad(50L, 10L, 5);

        assertTrue(updated);
        verify(jdbcTemplate).update(anyString(), eq(5), eq(50L), eq(10L));
    }

    @Test
    @DisplayName("deleteByIdPedidoAndIdProducto elimina correctamente por clave compuesta")
    void deleteByIdPedidoAndIdProducto_EliminaExitosamente() {
        when(jdbcTemplate.update(anyString(), eq(50L), eq(10L))).thenReturn(1);

        boolean deleted = repository.deleteByIdPedidoAndIdProducto(50L, 10L);

        assertTrue(deleted);
        verify(jdbcTemplate).update(anyString(), eq(50L), eq(10L));
    }

    @Test
    @DisplayName("deleteByPedidoId elimina todos los detalles de un pedido")
    void deleteByPedidoId_EliminaExitosamente() {
        when(jdbcTemplate.update(anyString(), eq(50L))).thenReturn(2);

        boolean deleted = repository.deleteByPedidoId(50L);

        assertTrue(deleted);
        verify(jdbcTemplate).update(anyString(), eq(50L));
    }

}
