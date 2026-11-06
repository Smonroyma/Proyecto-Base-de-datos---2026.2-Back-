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
import org.sabana.proyectofinal_bd1_back.model.dto.PagoMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.PagoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PagoResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;
import org.sabana.proyectofinal_bd1_back.model.entity.DetallePedido;
import org.sabana.proyectofinal_bd1_back.model.entity.EstadoFactura;
import org.sabana.proyectofinal_bd1_back.model.entity.EstadoPedido;
import org.sabana.proyectofinal_bd1_back.model.entity.Factura;
import org.sabana.proyectofinal_bd1_back.model.entity.Mesero;
import org.sabana.proyectofinal_bd1_back.model.entity.MetodoPago;
import org.sabana.proyectofinal_bd1_back.model.entity.Pago;
import org.sabana.proyectofinal_bd1_back.model.entity.Pedido;
import org.sabana.proyectofinal_bd1_back.model.entity.Staff;
import org.sabana.proyectofinal_bd1_back.repository.ClienteRepository;
import org.sabana.proyectofinal_bd1_back.repository.FacturaRepository;
import org.sabana.proyectofinal_bd1_back.repository.MesaRepository;
import org.sabana.proyectofinal_bd1_back.repository.MetodoPagoRepository;
import org.sabana.proyectofinal_bd1_back.repository.PagoFacturaRepository;
import org.sabana.proyectofinal_bd1_back.repository.PedidoRepository;
import org.sabana.proyectofinal_bd1_back.repository.UsuarioRepository;
import org.sabana.proyectofinal_bd1_back.util.email.EmailService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.FacturaNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.MetodoPagoNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.PagoInvalidoException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.PedidoNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.PedidoYaFacturadoException;

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
    private PagoMapper pagoMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private FacturaServiceImpl facturaService;

    private Cliente cliente;
    private Staff mesero;
    private Pedido pedido;
    private MetodoPago metodoEfectivo;
    private MetodoPago metodoTarjeta;
    private Factura facturaGuardada;
    private FacturaResponseDTO facturaResponseDTO;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .idCliente(1L)
                .nombre("Carlos")
                .apellido("Mendoza")
                .telefono("3001234567")
                .correo("carlos@example.com")
                .build();

        mesero = Mesero.builder()
                .idUsuario(2L)
                .nombres("Laura")
                .apellidos("Mesera")
                .correo("laura@savanacoffee.com")
                .rol("MESERO")
                .estado("ACTIVO")
                .build();

        DetallePedido detallePedido = DetallePedido.builder()
                .idProducto(10L)
                .nombreProducto("Café Americano")
                .cantidad(2)
                .precio(10000.0)
                .build();

        pedido = Pedido.builder()
                .idPedido(50L)
                .idCliente(1L)
                .nombreCliente("Carlos Mendoza")
                .numeroMesa(5)
                .idUsuario(2L)
                .nombreUsuario("Laura Mesera")
                .estado(EstadoPedido.ENTREGADO.name())
                .detalles(List.of(detallePedido))
                .build();

        metodoEfectivo = MetodoPago.builder()
                .tipo("EFECTIVO")
                .build();

        metodoTarjeta = MetodoPago.builder()
                .tipo("TARJETA")
                .build();

        facturaGuardada = Factura.builder()
                .numFactura("FAC-2026-00001")
                .idPedido(50L)
                .nombreCliente("Carlos Mendoza")
                .correoCliente("carlos.mendoza@email.com")
                .telefonoCliente("+57 300 1234567")
                .nombreUsuario("Laura Mesera")
                .fecha(LocalDateTime.now())
                .propina(2000.0)
                .impuesto(3800.0)
                .total(25800.0)
                .estado(EstadoFactura.PAGADA.name())
                .build();

        facturaResponseDTO = FacturaResponseDTO.builder()
                .numFactura("FAC-2026-00001")
                .idPedido(50L)
                .nombreCliente("Carlos Mendoza")
                .correoCliente("carlos@example.com")
                .telefonoCliente("3001234567")
                .nombreUsuario("Laura Mesera")
                .propina(2000.0)
                .impuesto(3800.0)
                .total(25800.0)
                .estado(EstadoFactura.PAGADA.name())
                .pagos(List.of(PagoResponseDTO.builder()
                        .idPago(10L)
                        .numFactura("FAC-2026-00001")
                        .tipoMetodoPago("EFECTIVO")
                        .estado("PAGADO")
                        .monto(25800.0)
                        .build()))
                .build();
    }

    @Test
    @DisplayName("procesarPagoYEmitirFactura calcula IVA del 19%, genera factura PAGADA, marca pedido PAGADO, libera mesa a DISPONIBLE y envía factura HTML por correo")
    void procesarPagoYEmitirFactura_Exitoso() {
        PagoRequestDTO pagoUnico = PagoRequestDTO.builder()
                .tipoMetodoPago("EFECTIVO")
                .monto(25800.0)
                .build();

        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .propina(2000.0)
                .pagos(List.of(pagoUnico))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(mesero));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(metodoPagoRepository.findByTipo("EFECTIVO")).thenReturn(Optional.of(metodoEfectivo));
        when(facturaRepository.generarSiguienteNumeroFactura()).thenReturn("FAC-2026-00001");
        when(facturaRepository.save(any(Factura.class))).thenReturn(facturaGuardada);
        when(facturaMapper.toResponseDTO(facturaGuardada)).thenReturn(facturaResponseDTO);

        FacturaResponseDTO result = facturaService.procesarPagoYEmitirFactura(request);

        assertNotNull(result);
        assertEquals("FAC-2026-00001", result.getNumFactura());
        assertEquals(3800.0, result.getImpuesto());
        assertEquals(25800.0, result.getTotal());
        assertEquals("PAGADA", result.getEstado());

        ArgumentCaptor<Factura> facturaCaptor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository).save(facturaCaptor.capture());
        assertEquals(3800.0, facturaCaptor.getValue().getImpuesto());
        assertEquals(25800.0, facturaCaptor.getValue().getTotal());

        verify(pagoFacturaRepository).saveBatch(anyList());
        verify(pedidoRepository).updateEstado(50L, EstadoPedido.PAGADO.name());
        verify(mesaRepository).updateEstado(5, "DISPONIBLE");
        verify(emailService).sendFacturaEmail("carlos@example.com", facturaResponseDTO);
    }

    @Test
    @DisplayName("procesarPagoYEmitirFactura con pagos mixtos (EFECTIVO + TARJETA) es exitoso")
    void procesarPagoYEmitirFactura_PagosMixtos_Exitoso() {
        PagoRequestDTO pagoEfectivo = PagoRequestDTO.builder()
                .tipoMetodoPago("EFECTIVO")
                .monto(10000.0)
                .build();

        PagoRequestDTO pagoTarjeta = PagoRequestDTO.builder()
                .tipoMetodoPago("TARJETA")
                .monto(15800.0)
                .build();

        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .propina(2000.0)
                .pagos(List.of(pagoEfectivo, pagoTarjeta))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(mesero));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(metodoPagoRepository.findByTipo("EFECTIVO")).thenReturn(Optional.of(metodoEfectivo));
        when(metodoPagoRepository.findByTipo("TARJETA")).thenReturn(Optional.of(metodoTarjeta));
        when(facturaRepository.generarSiguienteNumeroFactura()).thenReturn("FAC-2026-00002");
        when(facturaRepository.save(any(Factura.class))).thenReturn(facturaGuardada);
        when(facturaMapper.toResponseDTO(facturaGuardada)).thenReturn(facturaResponseDTO);

        FacturaResponseDTO result = facturaService.procesarPagoYEmitirFactura(request);

        assertNotNull(result);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Pago>> pagosCaptor = ArgumentCaptor.forClass(List.class);
        verify(pagoFacturaRepository).saveBatch(pagosCaptor.capture());
        assertEquals(2, pagosCaptor.getValue().size());
        verify(pedidoRepository).updateEstado(50L, EstadoPedido.PAGADO.name());
        verify(mesaRepository).updateEstado(5, "DISPONIBLE");
    }

    @Test
    @DisplayName("procesarPagoYEmitirFactura lanza PedidoNotFoundException cuando el pedido no existe")
    void procesarPagoYEmitirFactura_PedidoNoExiste_LanzaPedidoNotFoundException() {
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(999L)
                .pagos(List.of(PagoRequestDTO.builder().tipoMetodoPago("EFECTIVO").monto(23800.0).build()))
                .build();

        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PedidoNotFoundException.class, () -> facturaService.procesarPagoYEmitirFactura(request));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("procesarPagoYEmitirFactura lanza PedidoYaFacturadoException cuando el pedido ya tiene factura emitida")
    void procesarPagoYEmitirFactura_PedidoYaFacturadoEnBD_LanzaPedidoYaFacturadoException() {
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .pagos(List.of(PagoRequestDTO.builder().tipoMetodoPago("EFECTIVO").monto(23800.0).build()))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(true);

        assertThrows(PedidoYaFacturadoException.class, () -> facturaService.procesarPagoYEmitirFactura(request));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("procesarPagoYEmitirFactura lanza PagoInvalidoException cuando la suma de pagos no cuadra con el total (subtotal + 19% IVA + propina)")
    void procesarPagoYEmitirFactura_DescuadreDeMontos_LanzaPagoInvalidoException() {
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .propina(2000.0)
                .pagos(List.of(PagoRequestDTO.builder().tipoMetodoPago("EFECTIVO").monto(20000.0).build()))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(mesero));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(metodoPagoRepository.findByTipo("EFECTIVO")).thenReturn(Optional.of(metodoEfectivo));

        assertThrows(PagoInvalidoException.class, () -> facturaService.procesarPagoYEmitirFactura(request));
        verify(facturaRepository, never()).save(any());
        verify(pedidoRepository, never()).updateEstado(anyLong(), anyString());
    }

    @Test
    @DisplayName("procesarPagoYEmitirFactura lanza MetodoPagoNotFoundException cuando el tipo de método de pago no existe")
    void procesarPagoYEmitirFactura_MetodoPagoNoExiste_LanzaMetodoPagoNotFoundException() {
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .idPedido(50L)
                .propina(2000.0)
                .pagos(List.of(PagoRequestDTO.builder().tipoMetodoPago("CHEQUE").monto(25800.0).build()))
                .build();

        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.existsByPedidoId(50L)).thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(mesero));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(metodoPagoRepository.findByTipo("CHEQUE")).thenReturn(Optional.empty());

        assertThrows(MetodoPagoNotFoundException.class, () -> facturaService.procesarPagoYEmitirFactura(request));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("buscarPorNumFactura retorna FacturaResponseDTO cuando la factura existe")
    void buscarPorNumFactura_Exitoso() {
        when(facturaRepository.findByNumFactura("FAC-2026-00001")).thenReturn(Optional.of(facturaGuardada));
        when(facturaMapper.toResponseDTO(facturaGuardada)).thenReturn(facturaResponseDTO);

        FacturaResponseDTO result = facturaService.buscarPorNumFactura("FAC-2026-00001");

        assertNotNull(result);
        assertEquals("FAC-2026-00001", result.getNumFactura());
        verify(facturaRepository).findByNumFactura("FAC-2026-00001");
    }

    @Test
    @DisplayName("buscarPorNumFactura lanza FacturaNotFoundException cuando la factura no existe")
    void buscarPorNumFactura_NoExiste_LanzaFacturaNotFoundException() {
        when(facturaRepository.findByNumFactura("FAC-2026-99999")).thenReturn(Optional.empty());

        assertThrows(FacturaNotFoundException.class, () -> facturaService.buscarPorNumFactura("FAC-2026-99999"));
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
    @DisplayName("obtenerPagos retorna la lista de pagos filtrados por numFactura")
    void obtenerPagos_PorNumFactura_Exitoso() {
        Pago pago = Pago.builder()
                .idPago(10L)
                .numFactura("FAC-2026-00001")
                .tipoMetodoPago("EFECTIVO")
                .monto(25800.0)
                .estado("PAGADO")
                .build();
        when(pagoFacturaRepository.findByNumFactura("FAC-2026-00001")).thenReturn(List.of(pago));
        when(pagoMapper.toResponseDTOList(List.of(pago))).thenReturn(facturaResponseDTO.getPagos());

        List<PagoResponseDTO> result = facturaService.obtenerPagos("FAC-2026-00001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pagoFacturaRepository).findByNumFactura("FAC-2026-00001");
    }

}
