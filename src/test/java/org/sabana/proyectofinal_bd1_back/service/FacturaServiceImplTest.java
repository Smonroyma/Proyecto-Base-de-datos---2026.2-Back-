package org.sabana.proyectofinal_bd1_back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PagoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PagoResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;
import org.sabana.proyectofinal_bd1_back.model.entity.EstadoFactura;
import org.sabana.proyectofinal_bd1_back.model.entity.EstadoPedido;
import org.sabana.proyectofinal_bd1_back.model.entity.Factura;
import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;
import org.sabana.proyectofinal_bd1_back.model.entity.MetodoPago;
import org.sabana.proyectofinal_bd1_back.model.entity.PagoFactura;
import org.sabana.proyectofinal_bd1_back.model.entity.Pedido;
import org.sabana.proyectofinal_bd1_back.model.entity.Usuario;
import org.sabana.proyectofinal_bd1_back.repository.ClienteRepository;
import org.sabana.proyectofinal_bd1_back.repository.FacturaRepository;
import org.sabana.proyectofinal_bd1_back.repository.MesaRepository;
import org.sabana.proyectofinal_bd1_back.repository.MetodoPagoRepository;
import org.sabana.proyectofinal_bd1_back.repository.PagoFacturaRepository;
import org.sabana.proyectofinal_bd1_back.repository.PedidoRepository;
import org.sabana.proyectofinal_bd1_back.repository.UsuarioRepository;
import org.sabana.proyectofinal_bd1_back.util.email.EmailService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ClienteNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.FacturaNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.MetodoPagoNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.PagoInvalidoException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.PedidoNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.PedidoYaFacturadoException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.UsuarioNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaServiceImplTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private PagoFacturaRepository pagoFacturaRepository;

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MesaRepository mesaRepository;

    @Mock
    private FacturaMapper facturaMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private FacturaServiceImpl facturaService;

    private Cliente cliente;
    private Usuario usuario;
    private Mesa mesa;
    private Pedido pedido;
    private MetodoPago metodoEfectivo;
    private MetodoPago metodoTarjeta;
    private Factura facturaGuardada;
    private FacturaResponseDTO facturaResponseDTO;

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
                .nombre("Laura Cajera")
                .email("laura@savanacoffee.com")
                .rol("CAJERO")
                .build();

        mesa = Mesa.builder()
                .idMesa(3L)
                .numero(5)
                .capacidad(4)
                .estado("OCUPADA")
                .build();

        pedido = Pedido.builder()
                .idPedido(50L)
                .idCliente(1L)
                .nombreCliente("Carlos Mendoza")
                .idMesa(3L)
                .numeroMesa(5)
                .idUsuario(2L)
                .nombreUsuario("Laura Cajera")
                .estado(EstadoPedido.ENTREGADO.name())
                .tipoPedido("MESA")
                .total(25000.0)
                .build();

        metodoEfectivo = MetodoPago.builder()
                .idMetodoPago(1L)
                .codigo("EFECTIVO")
                .nombre("Efectivo")
                .activo(true)
                .build();

        metodoTarjeta = MetodoPago.builder()
                .idMetodoPago(2L)
                .codigo("TARJETA_CREDITO")
                .nombre("Tarjeta de Crédito")
                .activo(true)
                .build();

        // Subtotal = 25000.0, Impuesto (8%) = 2000.0, Propina = 2000.0, Descuento = 0.0 -> Total = 29000.0
        facturaGuardada = Factura.builder()
                .idFactura(100L)
                .numeroFactura("FAC-2026-00001")
                .idPedido(50L)
                .idCliente(1L)
                .nombreCliente("Carlos Mendoza")
                .identificacionCliente("3001234567")
                .idUsuario(2L)
                .nombreUsuario("Laura Cajera")
                .fechaEmision(LocalDateTime.now())
                .subtotal(25000.0)
                .propina(2000.0)
                .descuento(0.0)
                .impuesto(2000.0)
                .total(29000.0)
                .estado(EstadoFactura.PAGADA.name())
                .build();

        facturaResponseDTO = FacturaResponseDTO.builder()
                .idFactura(100L)
                .numeroFactura("FAC-2026-00001")
                .idPedido(50L)
                .idCliente(1L)
                .nombreCliente("Carlos Mendoza")
                .identificacionCliente("3001234567")
                .idUsuario(2L)
                .nombreUsuario("Laura Cajera")
                .subtotal(25000.0)
                .propina(2000.0)
                .descuento(0.0)
                .impuesto(2000.0)
                .total(29000.0)
                .estado(EstadoFactura.PAGADA.name())
                .pagos(List.of(PagoResponseDTO.builder()
                        .idPago(10L)
                        .idMetodoPago(1L)
                        .nombreMetodoPago("Efectivo")
                        .monto(29000.0)
                        .build()))
                .build();
    }

    @Test
    @DisplayName("emitirFactura con pago único procesa cobro, marca pedido PAGADO, libera mesa a DISPONIBLE y envía factura HTML por correo")
    void emitirFactura_PagoUnicoYMesa_Exitoso() {
        PagoRequestDTO pagoUnico = PagoRequestDTO.builder()
                .idMetodoPago(1L)
                .monto(29000.0)
                .referenciaTransaccion("CAJA-001")
                .build();

        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .idUsuario(2L)
                .propina(2000.0)
                .descuento(0.0)
                .pagos(List.of(pagoUnico))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoEfectivo));
        when(facturaRepository.generarSiguienteNumeroFactura()).thenReturn("FAC-2026-00001");
        when(facturaRepository.save(any(Factura.class))).thenReturn(facturaGuardada);
        when(mesaRepository.findById(3L)).thenReturn(Optional.of(mesa));
        when(facturaMapper.toResponseDTO(facturaGuardada)).thenReturn(facturaResponseDTO);

        FacturaResponseDTO result = facturaService.emitirFactura(request);

        assertNotNull(result);
        assertEquals(100L, result.getIdFactura());
        assertEquals("FAC-2026-00001", result.getNumeroFactura());
        assertEquals(29000.0, result.getTotal());
        assertEquals("PAGADA", result.getEstado());

        verify(facturaRepository).save(any(Factura.class));
        verify(pagoFacturaRepository).saveBatch(anyList());
        verify(pedidoRepository).updateEstado(50L, EstadoPedido.PAGADO.name());
        verify(emailService).sendFacturaEmail("carlos@example.com", facturaResponseDTO);

        ArgumentCaptor<Mesa> mesaCaptor = ArgumentCaptor.forClass(Mesa.class);
        verify(mesaRepository).update(mesaCaptor.capture());
        assertEquals("DISPONIBLE", mesaCaptor.getValue().getEstado());
    }

    @Test
    @DisplayName("emitirFactura con pagos mixtos (Efectivo + Tarjeta) y descuento en pedido para llevar es exitoso")
    void emitirFactura_PagosMixtosSinMesa_Exitoso() {
        pedido.setIdMesa(null);
        pedido.setTipoPedido("LLEVAR");

        // Subtotal = 25000.0, Impuesto = 2000.0, Propina = 1000.0, Descuento = 3000.0 -> Total = 25000.0
        PagoRequestDTO pagoEfectivo = PagoRequestDTO.builder()
                .idMetodoPago(1L)
                .monto(10000.0)
                .build();

        PagoRequestDTO pagoTarjeta = PagoRequestDTO.builder()
                .idMetodoPago(2L)
                .monto(15000.0)
                .referenciaTransaccion("VOUCHER-7788")
                .build();

        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .idUsuario(2L)
                .propina(1000.0)
                .descuento(3000.0)
                .pagos(List.of(pagoEfectivo, pagoTarjeta))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoEfectivo));
        when(metodoPagoRepository.findById(2L)).thenReturn(Optional.of(metodoTarjeta));
        when(facturaRepository.generarSiguienteNumeroFactura()).thenReturn("FAC-2026-00002");
        when(facturaRepository.save(any(Factura.class))).thenReturn(facturaGuardada);
        when(facturaMapper.toResponseDTO(facturaGuardada)).thenReturn(facturaResponseDTO);

        FacturaResponseDTO result = facturaService.emitirFactura(request);

        assertNotNull(result);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PagoFactura>> pagosCaptor = ArgumentCaptor.forClass(List.class);
        verify(pagoFacturaRepository).saveBatch(pagosCaptor.capture());
        assertEquals(2, pagosCaptor.getValue().size());
        verify(pedidoRepository).updateEstado(50L, EstadoPedido.PAGADO.name());
        verify(mesaRepository, never()).update(any());
    }

    @Test
    @DisplayName("emitirFactura lanza PedidoNotFoundException cuando el pedido no existe")
    void emitirFactura_PedidoNoExiste_LanzaPedidoNotFoundException() {
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(999L)
                .idUsuario(2L)
                .pagos(List.of(PagoRequestDTO.builder().idMetodoPago(1L).monto(27000.0).build()))
                .build();

        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PedidoNotFoundException.class, () -> facturaService.emitirFactura(request));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("emitirFactura lanza PedidoYaFacturadoException cuando el pedido ya tiene factura emitida")
    void emitirFactura_PedidoYaFacturadoEnBD_LanzaPedidoYaFacturadoException() {
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .idUsuario(2L)
                .pagos(List.of(PagoRequestDTO.builder().idMetodoPago(1L).monto(27000.0).build()))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(true);

        assertThrows(PedidoYaFacturadoException.class, () -> facturaService.emitirFactura(request));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("emitirFactura lanza PedidoYaFacturadoException cuando el pedido ya tiene estado PAGADO")
    void emitirFactura_PedidoEnEstadoPagado_LanzaPedidoYaFacturadoException() {
        pedido.setEstado(EstadoPedido.PAGADO.name());
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .idUsuario(2L)
                .pagos(List.of(PagoRequestDTO.builder().idMetodoPago(1L).monto(27000.0).build()))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);

        assertThrows(PedidoYaFacturadoException.class, () -> facturaService.emitirFactura(request));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("emitirFactura lanza PagoInvalidoException cuando el pedido se encuentra CANCELADO")
    void emitirFactura_PedidoCancelado_LanzaPagoInvalidoException() {
        pedido.setEstado(EstadoPedido.CANCELADO.name());
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .idUsuario(2L)
                .pagos(List.of(PagoRequestDTO.builder().idMetodoPago(1L).monto(27000.0).build()))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);

        assertThrows(PagoInvalidoException.class, () -> facturaService.emitirFactura(request));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("emitirFactura lanza UsuarioNotFoundException cuando el cajero no existe")
    void emitirFactura_UsuarioNoExiste_LanzaUsuarioNotFoundException() {
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .idUsuario(99L)
                .pagos(List.of(PagoRequestDTO.builder().idMetodoPago(1L).monto(27000.0).build()))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () -> facturaService.emitirFactura(request));
    }

    @Test
    @DisplayName("emitirFactura lanza ClienteNotFoundException cuando el cliente del pedido no existe")
    void emitirFactura_ClienteNoExiste_LanzaClienteNotFoundException() {
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .idUsuario(2L)
                .pagos(List.of(PagoRequestDTO.builder().idMetodoPago(1L).monto(27000.0).build()))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> facturaService.emitirFactura(request));
    }

    @Test
    @DisplayName("emitirFactura lanza MetodoPagoNotFoundException cuando el método de pago no existe")
    void emitirFactura_MetodoPagoNoExiste_LanzaMetodoPagoNotFoundException() {
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .idUsuario(2L)
                .propina(2000.0)
                .descuento(0.0)
                .pagos(List.of(PagoRequestDTO.builder().idMetodoPago(99L).monto(29000.0).build()))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(metodoPagoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MetodoPagoNotFoundException.class, () -> facturaService.emitirFactura(request));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("emitirFactura lanza PagoInvalidoException cuando el método de pago está inactivo")
    void emitirFactura_MetodoPagoInactivo_LanzaPagoInvalidoException() {
        metodoEfectivo.setActivo(false);
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .idUsuario(2L)
                .propina(2000.0)
                .descuento(0.0)
                .pagos(List.of(PagoRequestDTO.builder().idMetodoPago(1L).monto(29000.0).build()))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoEfectivo));

        assertThrows(PagoInvalidoException.class, () -> facturaService.emitirFactura(request));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("emitirFactura lanza PagoInvalidoException cuando la suma de pagos no cuadra con el total")
    void emitirFactura_DescuadreDeMontos_LanzaPagoInvalidoException() {
        // Total esperado = 29000.0, pero se envía un pago de 20000.0
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .idUsuario(2L)
                .propina(2000.0)
                .descuento(0.0)
                .pagos(List.of(PagoRequestDTO.builder().idMetodoPago(1L).monto(20000.0).build()))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoEfectivo));

        assertThrows(PagoInvalidoException.class, () -> facturaService.emitirFactura(request));
        verify(facturaRepository, never()).save(any());
        verify(pedidoRepository, never()).updateEstado(anyLong(), anyString());
    }

    @Test
    @DisplayName("emitirFactura lanza PagoInvalidoException cuando el descuento supera el subtotal más impuestos")
    void emitirFactura_DescuentoExcesivo_LanzaPagoInvalidoException() {
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .idUsuario(2L)
                .propina(0.0)
                .descuento(35000.0)
                .pagos(List.of(PagoRequestDTO.builder().idMetodoPago(1L).monto(1000.0).build()))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThrows(PagoInvalidoException.class, () -> facturaService.emitirFactura(request));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("buscarPorId retorna FacturaResponseDTO cuando la factura existe")
    void buscarPorId_Exitoso() {
        when(facturaRepository.findById(100L)).thenReturn(Optional.of(facturaGuardada));
        when(facturaMapper.toResponseDTO(facturaGuardada)).thenReturn(facturaResponseDTO);

        FacturaResponseDTO result = facturaService.buscarPorId(100L);

        assertNotNull(result);
        assertEquals(100L, result.getIdFactura());
        assertEquals("FAC-2026-00001", result.getNumeroFactura());
        verify(facturaRepository).findById(100L);
    }

    @Test
    @DisplayName("buscarPorId lanza FacturaNotFoundException cuando la factura no existe")
    void buscarPorId_NoExiste_LanzaFacturaNotFoundException() {
        when(facturaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(FacturaNotFoundException.class, () -> facturaService.buscarPorId(999L));
    }

    @Test
    @DisplayName("buscarPorPedidoId retorna FacturaResponseDTO cuando existe factura para el pedido")
    void buscarPorPedidoId_Exitoso() {
        when(facturaRepository.findByPedidoId(50L)).thenReturn(Optional.of(facturaGuardada));
        when(facturaMapper.toResponseDTO(facturaGuardada)).thenReturn(facturaResponseDTO);

        FacturaResponseDTO result = facturaService.buscarPorPedidoId(50L);

        assertNotNull(result);
        assertEquals(50L, result.getIdPedido());
        verify(facturaRepository).findByPedidoId(50L);
    }

    @Test
    @DisplayName("obtenerFacturas sin filtros retorna todas las facturas")
    void obtenerFacturas_SinFiltros_RetornaTodas() {
        List<Factura> lista = List.of(facturaGuardada);
        when(facturaRepository.findAll()).thenReturn(lista);
        when(facturaMapper.toResponseDTOList(lista)).thenReturn(List.of(facturaResponseDTO));

        List<FacturaResponseDTO> result = facturaService.obtenerFacturas(null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(facturaRepository).findAll();
    }

    @Test
    @DisplayName("obtenerFacturas con filtro de clienteId consulta por cliente")
    void obtenerFacturas_ConClienteId_RetornaFacturasCliente() {
        List<Factura> lista = List.of(facturaGuardada);
        when(facturaRepository.findByClienteId(1L)).thenReturn(lista);
        when(facturaMapper.toResponseDTOList(lista)).thenReturn(List.of(facturaResponseDTO));

        List<FacturaResponseDTO> result = facturaService.obtenerFacturas(1L, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(facturaRepository).findByClienteId(1L);
    }

    @Test
    @DisplayName("obtenerFacturas con rango de fechas consulta por rango y valida orden cronológico")
    void obtenerFacturas_ConRangoFechas_RetornaFacturasEnRango() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 1, 0, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 10, 31, 23, 59);
        List<Factura> lista = List.of(facturaGuardada);

        when(facturaRepository.findByRangoFechas(inicio, fin)).thenReturn(lista);
        when(facturaMapper.toResponseDTOList(lista)).thenReturn(List.of(facturaResponseDTO));

        List<FacturaResponseDTO> result = facturaService.obtenerFacturas(null, inicio, fin);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(facturaRepository).findByRangoFechas(inicio, fin);
    }

    @Test
    @DisplayName("obtenerFacturas lanza IllegalArgumentException cuando fechaFin es anterior a fechaInicio")
    void obtenerFacturas_RangoFechasInvalido_LanzaIllegalArgumentException() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 31, 0, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 10, 1, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> facturaService.obtenerFacturas(null, inicio, fin));
    }

    @Test
    @DisplayName("anularFactura actualiza el estado a ANULADA exitosamente")
    void anularFactura_Exitoso() {
        FacturaResponseDTO anuladaResponse = FacturaResponseDTO.builder()
                .idFactura(100L)
                .numeroFactura("FAC-2026-00001")
                .estado(EstadoFactura.ANULADA.name())
                .build();

        when(facturaRepository.findById(100L)).thenReturn(Optional.of(facturaGuardada));
        when(facturaRepository.updateEstado(100L, EstadoFactura.ANULADA.name())).thenReturn(true);
        when(facturaMapper.toResponseDTO(facturaGuardada)).thenReturn(anuladaResponse);

        FacturaResponseDTO result = facturaService.anularFactura(100L);

        assertNotNull(result);
        assertEquals(EstadoFactura.ANULADA.name(), result.getEstado());
        assertEquals(EstadoFactura.ANULADA.name(), facturaGuardada.getEstado());
        verify(facturaRepository).updateEstado(100L, EstadoFactura.ANULADA.name());
    }

    @Test
    @DisplayName("anularFactura lanza FacturaNotFoundException cuando la factura no existe")
    void anularFactura_NoExiste_LanzaFacturaNotFoundException() {
        when(facturaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(FacturaNotFoundException.class, () -> facturaService.anularFactura(999L));
        verify(facturaRepository, never()).updateEstado(anyLong(), anyString());
    }

    @Test
    @DisplayName("anularFactura lanza IllegalStateException cuando la factura ya está ANULADA")
    void anularFactura_YaAnulada_LanzaIllegalStateException() {
        facturaGuardada.setEstado(EstadoFactura.ANULADA.name());
        when(facturaRepository.findById(100L)).thenReturn(Optional.of(facturaGuardada));

        assertThrows(IllegalStateException.class, () -> facturaService.anularFactura(100L));
        verify(facturaRepository, never()).updateEstado(anyLong(), anyString());
    }

}
