package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.FacturaMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PagoMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.PagoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PagoResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;
import org.sabana.proyectofinal_bd1_back.model.entity.EstadoFactura;
import org.sabana.proyectofinal_bd1_back.model.entity.EstadoPedido;
import org.sabana.proyectofinal_bd1_back.model.entity.Factura;
import org.sabana.proyectofinal_bd1_back.model.entity.MetodoPago;
import org.sabana.proyectofinal_bd1_back.model.entity.PagoFactura;
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
import org.sabana.proyectofinal_bd1_back.util.exceptions.ClienteNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.FacturaNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.MetodoPagoNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.PagoInvalidoException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.PedidoNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.PedidoYaFacturadoException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.UsuarioNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de la lógica de negocio para el flujo V2 Pago -> Factura con 19% IVA.
 */
@Service
public class FacturaServiceImpl implements FacturaService {

    public static final double PORCENTAJE_IVA = 0.19;

    private final FacturaRepository facturaRepository;
    private final PagoFacturaRepository pagoFacturaRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MesaRepository mesaRepository;
    private final FacturaMapper facturaMapper;
    private final PagoMapper pagoMapper;
    private final EmailService emailService;

    public FacturaServiceImpl(FacturaRepository facturaRepository,
                              PagoFacturaRepository pagoFacturaRepository,
                              MetodoPagoRepository metodoPagoRepository,
                              PedidoRepository pedidoRepository,
                              ClienteRepository clienteRepository,
                              UsuarioRepository usuarioRepository,
                              MesaRepository mesaRepository,
                              FacturaMapper facturaMapper,
                              PagoMapper pagoMapper,
                              EmailService emailService) {
        this.facturaRepository = facturaRepository;
        this.pagoFacturaRepository = pagoFacturaRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.mesaRepository = mesaRepository;
        this.facturaMapper = facturaMapper;
        this.pagoMapper = pagoMapper;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public FacturaResponseDTO procesarPagoYEmitirFactura(FacturaRequestDTO requestDTO) {
        Pedido pedido = pedidoRepository.findById(requestDTO.getIdPedido())
                .orElseThrow(() -> new PedidoNotFoundException(requestDTO.getIdPedido()));

        if (facturaRepository.existsByPedidoId(pedido.getIdPedido()) ||
                EstadoPedido.PAGADO.name().equalsIgnoreCase(pedido.getEstado())) {
            throw new PedidoYaFacturadoException(pedido.getIdPedido());
        }

        if (EstadoPedido.CANCELADO.name().equalsIgnoreCase(pedido.getEstado())) {
            throw new PagoInvalidoException(String.format(
                    "No se puede procesar el pago del pedido con ID %d porque se encuentra en estado CANCELADO",
                    pedido.getIdPedido()));
        }

        Long idUsuarioEfectivo = requestDTO.getIdUsuario() != null ? requestDTO.getIdUsuario() : pedido.getIdUsuario();
        Staff staff = usuarioRepository.findById(idUsuarioEfectivo)
                .orElseThrow(() -> new UsuarioNotFoundException(idUsuarioEfectivo));

        Cliente cliente = clienteRepository.findById(pedido.getIdCliente())
                .orElseThrow(() -> new ClienteNotFoundException(pedido.getIdCliente()));

        List<PagoRequestDTO> itemsPago = requestDTO.resolvePagos();
        if (itemsPago == null || itemsPago.isEmpty()) {
            throw new PagoInvalidoException("Debe registrar al menos un método de pago para procesar el cobro del pedido");
        }

        double propina = requestDTO.getPropina() != null ? requestDTO.getPropina() : 0.0;
        if (propina < 0.0) {
            throw new PagoInvalidoException("El monto de la propina no puede ser negativo");
        }

        double subtotal = pedido.getSubtotal() != null ? pedido.getSubtotal() : 0.0;
        double impuesto = Math.round(subtotal * PORCENTAJE_IVA * 100.0) / 100.0;
        double totalCalculado = Math.round((subtotal + impuesto + propina) * 100.0) / 100.0;

        double sumaPagos = 0.0;
        List<PagoFactura> pagosList = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();

        for (PagoRequestDTO pagoReq : itemsPago) {
            if (pagoReq.getTotal() == null || pagoReq.getTotal() <= 0.0) {
                throw new PagoInvalidoException("Cada pago registrado debe tener un monto mayor a cero");
            }
            if (pagoReq.getTipoMetodoPago() == null || pagoReq.getTipoMetodoPago().isBlank()) {
                throw new PagoInvalidoException("El tipo de método de pago es obligatorio (EFECTIVO, TARJETA, TRANSFERENCIA)");
            }

            MetodoPago metodoPago = metodoPagoRepository.findByTipo(pagoReq.getTipoMetodoPago())
                    .orElseThrow(() -> new MetodoPagoNotFoundException(
                            String.format("No se encontró el método de pago de tipo: '%s'", pagoReq.getTipoMetodoPago())));

            if (!Boolean.TRUE.equals(metodoPago.getActivo())) {
                throw new PagoInvalidoException(String.format(
                        "El método de pago '%s' (tipo: %s) no se encuentra activo para procesar transacciones",
                        metodoPago.getNombre(), metodoPago.getTipo()));
            }

            sumaPagos += pagoReq.getTotal();

            PagoFactura pagoFactura = PagoFactura.builder()
                    .tipoMetodoPago(metodoPago.getTipo())
                    .nombreMetodoPago(metodoPago.getNombre())
                    .total(pagoReq.getTotal())
                    .estado("PAGADO")
                    .referenciaTransaccion(pagoReq.getReferenciaTransaccion())
                    .fecha(ahora)
                    .build();

            pagosList.add(pagoFactura);
        }

        sumaPagos = Math.round(sumaPagos * 100.0) / 100.0;

        if (Math.abs(sumaPagos - totalCalculado) > 0.01) {
            throw new PagoInvalidoException(String.format(
                    "Descuadre de caja: la suma de los pagos recibidos (%.2f) no coincide con el total calculado de la factura con 19%% IVA (%.2f)",
                    sumaPagos, totalCalculado));
        }

        String numFactura = facturaRepository.generarSiguienteNumeroFactura();

        Factura factura = Factura.builder()
                .numFactura(numFactura)
                .idPedido(pedido.getIdPedido())
                .idCliente(cliente.getIdCliente())
                .nombreCliente(cliente.getNombreCompleto())
                .correoCliente(cliente.getCorreo())
                .telefonoCliente(cliente.getTelefono())
                .idUsuario(staff.getIdUsuario())
                .nombreUsuario(staff.getNombreCompleto())
                .fecha(ahora)
                .estado(EstadoFactura.PAGADA.name())
                .subtotal(subtotal)
                .propina(propina)
                .impuesto(impuesto)
                .total(totalCalculado)
                .build();

        Factura facturaGuardada = facturaRepository.save(factura);

        for (PagoFactura pago : pagosList) {
            pago.setNumFactura(facturaGuardada.getNumFactura());
        }

        pagoFacturaRepository.saveBatch(pagosList);
        facturaGuardada.setPagos(pagosList);

        pedidoRepository.updateEstado(pedido.getIdPedido(), EstadoPedido.PAGADO.name());

        if (pedido.getNumeroMesa() != null) {
            mesaRepository.updateEstado(pedido.getNumeroMesa(), "DISPONIBLE");
        }

        FacturaResponseDTO responseDTO = facturaMapper.toResponseDTO(facturaGuardada);

        if (cliente.getCorreo() != null && !cliente.getCorreo().isBlank()) {
            emailService.sendFacturaEmail(cliente.getCorreo(), responseDTO);
        }

        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponseDTO buscarPorNumFactura(String numFactura) {
        Factura factura = facturaRepository.findByNumFactura(numFactura)
                .orElseThrow(() -> new FacturaNotFoundException(
                        String.format("No se encontró la factura con número: %s", numFactura)));
        return facturaMapper.toResponseDTO(factura);
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponseDTO buscarPorPedidoId(Long idPedido) {
        Factura factura = facturaRepository.findByPedidoId(idPedido)
                .orElseThrow(() -> new FacturaNotFoundException(
                        String.format("No se encontró factura emitida para el pedido con ID: %d", idPedido)));
        return facturaMapper.toResponseDTO(factura);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> obtenerFacturas(Long clienteId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        List<Factura> facturas;

        if (clienteId != null) {
            facturas = facturaRepository.findByClienteId(clienteId);
            if (fechaInicio != null && fechaFin != null) {
                facturas = facturas.stream()
                        .filter(f -> f.getFecha() != null
                                && !f.getFecha().isBefore(fechaInicio)
                                && !f.getFecha().isAfter(fechaFin))
                        .toList();
            }
        } else if (fechaInicio != null && fechaFin != null) {
            facturas = facturaRepository.findByRangoFechas(fechaInicio, fechaFin);
        } else {
            facturas = facturaRepository.findAll();
        }

        return facturaMapper.toResponseDTOList(facturas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> obtenerPagos(String numFactura) {
        List<PagoFactura> pagos;
        if (numFactura != null && !numFactura.isBlank()) {
            pagos = pagoFacturaRepository.findByNumFactura(numFactura.trim());
        } else {
            pagos = pagoFacturaRepository.findAll();
        }
        return pagoMapper.toResponseDTOList(pagos);
    }

}
