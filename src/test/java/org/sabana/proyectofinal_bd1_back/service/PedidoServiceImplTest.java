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
import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;
import org.sabana.proyectofinal_bd1_back.model.entity.Pedido;
import org.sabana.proyectofinal_bd1_back.model.entity.Producto;
import org.sabana.proyectofinal_bd1_back.model.entity.Usuario;
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
import org.sabana.proyectofinal_bd1_back.util.exceptions.UserNotFoundException;

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
    private Usuario usuario;
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
                .nombre("Carlos Mendoza")
                .telefono("3001234567")
                .email("carlos@example.com")
                .build();

        usuario = Usuario.builder()
                .idUsuario(2L)
                .nombre("Ana Barista")
                .email("ana@savanacoffee.com")
                .rol("BARISTA")
                .build();

        mesa = Mesa.builder()
                .idMesa(3L)
                .numero(5)
                .capacidad(4)
                .estado("DISPONIBLE")
                .build();

        producto = Producto.builder()
                .idProducto(10L)
                .nombre("Café Americano")
                .precio(6500.0)
                .idCategoria(1L)
                .disponible(true)
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
                .idMesa(3L)
                .numeroMesa(5)
                .idUsuario(2L)
                .nombreUsuario("Ana Barista")
                .fechaHora(LocalDateTime.now())
                .estado("PENDIENTE")
                .tipoPedido("MESA")
                .total(13000.0)
                .observaciones("Sin azúcar")
                .detalles(new ArrayList<>(List.of(detalle)))
                .build();

        requestDTO = PedidoRequestDTO.builder()
                .idCliente(1L)
                .idMesa(3L)
                .idUsuario(2L)
                .tipoPedido("MESA")
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
                .idMesa(3L)
                .numeroMesa(5)
                .idUsuario(2L)
                .nombreUsuario("Ana Barista")
                .fechaHora(pedido.getFechaHora())
                .estado("PENDIENTE")
                .tipoPedido("MESA")
                .total(13000.0)
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
    @DisplayName("crearPedido registra orden exitosamente, descuenta stock y calcula total")
    void crearPedido_Exitoso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(mesaRepository.findById(3L)).thenReturn(Optional.of(mesa));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.updateStock(eq(10L), eq(18))).thenReturn(true);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(responseDTO);

        PedidoResponseDTO result = pedidoService.crearPedido(requestDTO);

        assertNotNull(result);
        assertEquals(50L, result.getIdPedido());
        assertEquals(13000.0, result.getTotal());
        assertEquals("MESA", result.getTipoPedido());

        verify(productoRepository).updateStock(10L, 18);
        verify(mesaRepository).update(any(Mesa.class));
        verify(pedidoRepository).save(any(Pedido.class));
        verify(detallePedidoRepository).saveBatch(any());
    }

    @Test
    @DisplayName("crearPedido lanza ClienteNotFoundException cuando el cliente no existe")
    void crearPedido_ClienteNoExiste_LanzaClienteNotFoundException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza UserNotFoundException cuando el usuario no existe")
    void crearPedido_UsuarioNoExiste_LanzaUserNotFoundException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza MesaNotFoundException cuando la mesa especificada no existe")
    void crearPedido_MesaNoExiste_LanzaMesaNotFoundException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(mesaRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(MesaNotFoundException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza IllegalStateException cuando la mesa está en mantenimiento")
    void crearPedido_MesaEnMantenimiento_LanzaIllegalStateException() {
        mesa.setEstado("EN_MANTENIMIENTO");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(mesaRepository.findById(3L)).thenReturn(Optional.of(mesa));

        assertThrows(IllegalStateException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza IllegalArgumentException cuando el tipo de pedido es inválido")
    void crearPedido_TipoPedidoInvalido_LanzaIllegalArgumentException() {
        requestDTO.setTipoPedido("DOMICILIO");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        assertThrows(IllegalArgumentException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza IllegalArgumentException cuando es tipo MESA pero idMesa es nulo")
    void crearPedido_MesaSinIdMesa_LanzaIllegalArgumentException() {
        requestDTO.setIdMesa(null);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        assertThrows(IllegalArgumentException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza ProductoNotFoundException cuando un producto de los detalles no existe")
    void crearPedido_ProductoNoExiste_LanzaProductoNotFoundException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(mesaRepository.findById(3L)).thenReturn(Optional.of(mesa));
        when(productoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(productoRepository, never()).updateStock(anyLong(), anyInt());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido lanza StockInsuficienteException cuando el producto no está disponible")
    void crearPedido_ProductoNoDisponible_LanzaStockInsuficienteException() {
        producto.setDisponible(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(mesaRepository.findById(3L)).thenReturn(Optional.of(mesa));
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
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(mesaRepository.findById(3L)).thenReturn(Optional.of(mesa));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThrows(StockInsuficienteException.class, () -> pedidoService.crearPedido(requestDTO));
        verify(productoRepository, never()).updateStock(anyLong(), anyInt());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido para LLEVAR no requiere mesa y se procesa exitosamente")
    void crearPedido_ParaLlevar_Exitoso() {
        requestDTO.setTipoPedido("LLEVAR");
        requestDTO.setIdMesa(null);
        pedido.setTipoPedido("LLEVAR");
        pedido.setIdMesa(null);
        pedido.setNumeroMesa(null);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(responseDTO);

        PedidoResponseDTO result = pedidoService.crearPedido(requestDTO);

        assertNotNull(result);
        verify(mesaRepository, never()).findById(anyLong());
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("buscarPorId retorna PedidoResponseDTO cuando el pedido existe")
    void buscarPorId_Exitoso() {
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(responseDTO);

        PedidoResponseDTO result = pedidoService.buscarPorId(50L);

        assertNotNull(result);
        assertEquals(50L, result.getIdPedido());
        verify(pedidoRepository).findById(50L);
    }

    @Test
    @DisplayName("buscarPorId lanza PedidoNotFoundException cuando el pedido no existe")
    void buscarPorId_NoExiste_LanzaPedidoNotFoundException() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PedidoNotFoundException.class, () -> pedidoService.buscarPorId(99L));
        verify(pedidoRepository).findById(99L);
    }

    @Test
    @DisplayName("obtenerPedidos sin filtros retorna todos los pedidos")
    void obtenerPedidos_SinFiltros_RetornaTodos() {
        List<Pedido> pedidos = List.of(pedido);
        List<PedidoResponseDTO> dtoList = List.of(responseDTO);

        when(pedidoRepository.findAll()).thenReturn(pedidos);
        when(pedidoMapper.toResponseDTOList(pedidos)).thenReturn(dtoList);

        List<PedidoResponseDTO> result = pedidoService.obtenerPedidos(null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pedidoRepository).findAll();
    }

    @Test
    @DisplayName("obtenerPedidos con filtro de cliente retorna pedidos de dicho cliente")
    void obtenerPedidos_ConFiltroCliente_RetornaFiltrados() {
        List<Pedido> pedidos = List.of(pedido);
        List<PedidoResponseDTO> dtoList = List.of(responseDTO);

        when(pedidoRepository.findByCliente(1L)).thenReturn(pedidos);
        when(pedidoMapper.toResponseDTOList(pedidos)).thenReturn(dtoList);

        List<PedidoResponseDTO> result = pedidoService.obtenerPedidos(1L, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pedidoRepository).findByCliente(1L);
    }

    @Test
    @DisplayName("actualizarEstado modifica el estado correctamente cuando es una transición válida")
    void actualizarEstado_Exitoso() {
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.updateEstado(50L, "EN_PREPARACION")).thenReturn(true);
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(responseDTO);

        PedidoResponseDTO result = pedidoService.actualizarEstado(50L, "EN_PREPARACION");

        assertNotNull(result);
        assertEquals("EN_PREPARACION", pedido.getEstado());
        verify(pedidoRepository).updateEstado(50L, "EN_PREPARACION");
    }

    @Test
    @DisplayName("actualizarEstado lanza IllegalArgumentException cuando el estado no es válido")
    void actualizarEstado_EstadoInvalido_LanzaIllegalArgumentException() {
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class, () -> pedidoService.actualizarEstado(50L, "ESTADO_INEXISTENTE"));
        verify(pedidoRepository, never()).updateEstado(anyLong(), any());
    }

    @Test
    @DisplayName("actualizarEstado lanza IllegalStateException cuando el pedido ya está en estado terminal CANCELADO o PAGADO")
    void actualizarEstado_EstadoTerminal_LanzaIllegalStateException() {
        pedido.setEstado("PAGADO");
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> pedidoService.actualizarEstado(50L, "EN_PREPARACION"));
        verify(pedidoRepository, never()).updateEstado(anyLong(), any());
    }

    @Test
    @DisplayName("eliminarPedido borra el pedido cuando existe")
    void eliminarPedido_Exitoso() {
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));

        pedidoService.eliminarPedido(50L);

        verify(pedidoRepository).deleteById(50L);
    }

    @Test
    @DisplayName("eliminarPedido lanza PedidoNotFoundException cuando el pedido no existe")
    void eliminarPedido_NoExiste_LanzaPedidoNotFoundException() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PedidoNotFoundException.class, () -> pedidoService.eliminarPedido(99L));
        verify(pedidoRepository, never()).deleteById(anyLong());
    }

}
