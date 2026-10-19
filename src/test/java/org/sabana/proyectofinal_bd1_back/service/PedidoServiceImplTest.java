package org.sabana.proyectofinal_bd1_back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sabana.proyectofinal_bd1_back.model.dto.DetallePedidoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.DetallePedidoResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PedidoMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.PedidoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PedidoResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;
import org.sabana.proyectofinal_bd1_back.model.entity.DetallePedido;
import org.sabana.proyectofinal_bd1_back.model.entity.Empleado;
import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;
import org.sabana.proyectofinal_bd1_back.model.entity.Pedido;
import org.sabana.proyectofinal_bd1_back.model.entity.Producto;
import org.sabana.proyectofinal_bd1_back.model.entity.Staff;
import org.sabana.proyectofinal_bd1_back.repository.ClienteRepository;
import org.sabana.proyectofinal_bd1_back.repository.DetallePedidoRepository;
import org.sabana.proyectofinal_bd1_back.repository.MesaRepository;
import org.sabana.proyectofinal_bd1_back.repository.PedidoRepository;
import org.sabana.proyectofinal_bd1_back.repository.ProductoRepository;
import org.sabana.proyectofinal_bd1_back.repository.UsuarioRepository;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ClienteNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.MesaNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.PedidoNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ProductoNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.StockInsuficienteException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.UsuarioNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private MesaRepository mesaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PedidoMapper pedidoMapper;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Cliente cliente;
    private Staff empleado;
    private Mesa mesa;
    private Producto producto;
    private Pedido pedido;
    private DetallePedido detalle;
    private PedidoRequestDTO requestDTO;
    private PedidoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .idCliente(1L)
                .nombres("Carlos")
                .apellidos("Mendoza")
                .telefono("3001234567")
                .correo("carlos@example.com")
                .build();

        empleado = Empleado.builder()
                .idUsuario(2L)
                .nombres("Ana")
                .apellidos("Mesera")
                .correo("ana@savanacoffee.com")
                .rol("EMPLEADO")
                .estado("ACTIVO")
                .build();

        mesa = Mesa.builder()
                .numeroMesa(5)
                .capacidad(4)
                .estado("DISPONIBLE")
                .build();

        producto = Producto.builder()
                .idProducto(10L)
                .nombre("Café Americano")
                .precio(6500.0)
                .nombreCategoria("Bebidas Calientes")
                .disponibilidad(true)
                .stock(20)
                .build();

        detalle = DetallePedido.builder()
                .idDetallePedido(100L)
                .idPedido(50L)
                .idProducto(10L)
                .nombreProducto("Café Americano")
                .cantidad(2)
                .precioUnitario(6500.0)
                .subtotal(13000.0)
                .build();

        pedido = Pedido.builder()
                .idPedido(50L)
                .idCliente(1L)
                .nombreCliente("Carlos Mendoza")
                .numeroMesa(5)
                .idUsuario(2L)
                .nombreUsuario("Ana Mesera")
                .fecha(LocalDateTime.now())
                .estado("PENDIENTE")
                .subtotal(13000.0)
                .observaciones("Sin azúcar")
                .detalles(new ArrayList<>(List.of(detalle)))
                .build();

        requestDTO = PedidoRequestDTO.builder()
                .idCliente(1L)
                .numeroMesa(5)
                .idUsuario(2L)
                .observaciones("Sin azúcar")
                .detalles(List.of(
                        DetallePedidoRequestDTO.builder()
                                .idProducto(10L)
                                .cantidad(2)
                                .build()
                ))
                .build();

        responseDTO = PedidoResponseDTO.builder()
                .idPedido(50L)
                .idCliente(1L)
                .nombreCliente("Carlos Mendoza")
                .numeroMesa(5)
                .idUsuario(2L)
                .nombreUsuario("Ana Mesera")
                .fecha(pedido.getFecha())
                .estado("PENDIENTE")
                .subtotal(13000.0)
                .observaciones("Sin azúcar")
                .detalles(List.of(
                        DetallePedidoResponseDTO.builder()
                                .idDetallePedido(100L)
                                .idProducto(10L)
                                .nombreProducto("Café Americano")
                                .cantidad(2)
                                .precioUnitario(6500.0)
                                .subtotal(13000.0)
                                .build()
                ))
                .build();
    }

    @Test
    @DisplayName("crearPedido registra orden en mesa exitosamente, descuenta stock y calcula subtotal")
    void crearPedido_Exitoso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(empleado));
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.updateStock(eq(10L), eq(18))).thenReturn(true);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(responseDTO);

        PedidoResponseDTO result = pedidoService.crearPedido(requestDTO);

        assertNotNull(result);
        assertEquals(50L, result.getIdPedido());
        assertEquals(13000.0, result.getSubtotal());
        assertEquals(5, result.getNumeroMesa());

        verify(productoRepository).updateStock(10L, 18);
        verify(mesaRepository).update(any(Mesa.class));
        verify(pedidoRepository).save(any(Pedido.class));
        verify(detallePedidoRepository).saveBatch(any());
    }

    @Test
    @DisplayName("crearPedido lanza IllegalArgumentException cuando numeroMesa es nulo (prohibidos pedidos para llevar)")
    void crearPedido_SinMesa_LanzaIllegalArgumentException() {
        requestDTO.setNumeroMesa(null);

        assertThrows(IllegalArgumentException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza ClienteNotFoundException cuando el cliente no existe")
    void crearPedido_ClienteNoExiste_LanzaClienteNotFoundException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza UsuarioNotFoundException cuando el miembro de Staff no existe")
    void crearPedido_UsuarioNoExiste_LanzaUsuarioNotFoundException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza MesaNotFoundException cuando la mesa especificada no existe")
    void crearPedido_MesaNoExiste_LanzaMesaNotFoundException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(empleado));
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.empty());

        assertThrows(MesaNotFoundException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza IllegalStateException cuando la mesa está en mantenimiento")
    void crearPedido_MesaEnMantenimiento_LanzaIllegalStateException() {
        mesa.setEstado("EN_MANTENIMIENTO");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(empleado));
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));

        assertThrows(IllegalStateException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza ProductoNotFoundException cuando un producto de los detalles no existe")
    void crearPedido_ProductoNoExiste_LanzaProductoNotFoundException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(empleado));
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));
        when(productoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(productoRepository, never()).updateStock(anyLong(), anyInt());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza StockInsuficienteException cuando el producto no está disponible")
    void crearPedido_ProductoNoDisponible_LanzaStockInsuficienteException() {
        producto.setDisponibilidad(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(empleado));
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThrows(StockInsuficienteException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(productoRepository, never()).updateStock(anyLong(), anyInt());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza StockInsuficienteException cuando el stock es insuficiente")
    void crearPedido_StockInsuficiente_LanzaStockInsuficienteException() {
        producto.setStock(1);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(empleado));
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThrows(StockInsuficienteException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(productoRepository, never()).updateStock(anyLong(), anyInt());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarEstado permite transición unidireccional PENDIENTE -> EN_PREPARACION -> ENTREGADO")
    void actualizarEstado_TransicionUnidireccional_Exitoso() {
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.updateEstado(50L, "EN_PREPARACION")).thenReturn(true);
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(responseDTO);

        PedidoResponseDTO result1 = pedidoService.actualizarEstado(50L, "EN_PREPARACION");
        assertNotNull(result1);
        assertEquals("EN_PREPARACION", pedido.getEstado());

        when(pedidoRepository.updateEstado(50L, "ENTREGADO")).thenReturn(true);
        PedidoResponseDTO result2 = pedidoService.actualizarEstado(50L, "ENTREGADO");
        assertNotNull(result2);
        assertEquals("ENTREGADO", pedido.getEstado());
    }

    @Test
    @DisplayName("actualizarEstado lanza IllegalArgumentException (400 Bad Request) al intentar retroceder de estado")
    void actualizarEstado_RetrocesoDeEstado_LanzaIllegalArgumentException() {
        pedido.setEstado("ENTREGADO");
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class, () -> pedidoService.actualizarEstado(50L, "EN_PREPARACION"));
        assertThrows(IllegalArgumentException.class, () -> pedidoService.actualizarEstado(50L, "PENDIENTE"));
        verify(pedidoRepository, never()).updateEstado(anyLong(), any());
    }

    @Test
    @DisplayName("actualizarEstado lanza IllegalArgumentException cuando el pedido ya está en estado terminal PAGADO")
    void actualizarEstado_EstadoTerminal_LanzaIllegalArgumentException() {
        pedido.setEstado("PAGADO");
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class, () -> pedidoService.actualizarEstado(50L, "EN_PREPARACION"));
        verify(pedidoRepository, never()).updateEstado(anyLong(), any());
    }

    @Test
    @DisplayName("eliminarPedido borra el pedido cuando existe")
    void eliminarPedido_Exitoso() {
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));

        pedidoService.eliminarPedido(50L);

        verify(pedidoRepository).deleteById(50L);
    }

}
