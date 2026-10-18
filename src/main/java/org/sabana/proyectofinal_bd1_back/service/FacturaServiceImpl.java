package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.FacturaMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PagoRequestDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de la lógica de negocio para la gestión transaccional de Facturas y Pagos.
 */
@Service
public class FacturaServiceImpl implements FacturaService {

    public static final double PORCENTAJE_IMPUESTO = 0.08;

    private final FacturaRepository facturaRepository;
    private final PagoFacturaRepository pagoFacturaRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MesaRepository mesaRepository;
    private final FacturaMapper facturaMapper;
    private final EmailService emailService;

    public FacturaServiceImpl(FacturaRepository facturaRepository,
                              PagoFacturaRepository pagoFacturaRepository,
                              MetodoPagoRepository metodoPagoRepository,
                              PedidoRepository pedidoRepository,
                              ClienteRepository clienteRepository,
                              UsuarioRepository usuarioRepository,
                              MesaRepository mesaRepository,
                              FacturaMapper facturaMapper,
                              EmailService emailService) {
        this.facturaRepository = facturaRepository;
        this.pagoFacturaRepository = pagoFacturaRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.mesaRepository = mesaRepository;
        this.facturaMapper = facturaMapper;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public FacturaResponseDTO emitirFactura(FacturaRequestDTO requestDTO) {
        Pedido pedido = pedidoRepository.findById(requestDTO.getIdPedido())
                .orElseThrow(() -> new PedidoNotFoundException(requestDTO.getIdPedido()));

        if (facturaRepository.existsByPedidoId(pedido.getIdPedido()) ||
                EstadoPedido.PAGADO.name().equalsIgnoreCase(pedido.getEstado())) {
            throw new PedidoYaFacturadoException(pedido.getIdPedido());
        }

        if (EstadoPedido.CANCELADO.name().equalsIgnoreCase(pedido.getEstado())) {
            throw new PagoInvalidoException(String.format(
                    "No se puede facturar el pedido con ID %d porque se encuentra en estado CANCELADO",
                    pedido.getIdPedido()));
        }

        Usuario usuario = usuarioRepository.findById(requestDTO.getIdUsuario())
                .orElseThrow(() -> new UsuarioNotFoundException(requestDTO.getIdUsuario()));

        Cliente cliente = clienteRepository.findById(pedido.getIdCliente())
                .orElseThrow(() -> new ClienteNotFoundException(pedido.getIdCliente()));

        if (requestDTO.getPagos() == null || requestDTO.getPagos().isEmpty()) {
            throw new PagoInvalidoException("Debe registrar al menos un medio de pago para emitir la factura");
        }

        double propina = requestDTO.getPropina() != null ? requestDTO.getPropina() : 0.0;
        double descuento = requestDTO.getDescuento() != null ? requestDTO.getDescuento() : 0.0;

        if (propina < 0.0) {
            throw new PagoInvalidoException("El monto de la propina no puede ser negativo");
        }
        if (descuento < 0.0) {
            throw new PagoInvalidoException("El monto del descuento no puede ser negativo");
        }

        double subtotal = pedido.getTotal() != null ? pedido.getTotal() : 0.0;
        double impuesto = Math.round(subtotal * PORCENTAJE_IMPUESTO * 100.0) / 100.0;

        if (descuento > (subtotal + impuesto)) {
            throw new PagoInvalidoException("El descuento no puede superar el subtotal más impuestos de la factura");
        }

        double totalCalculado = Math.round((subtotal + impuesto + propina - descuento) * 100.0) / 100.0;

        double sumaPagos = 0.0;
        List<PagoFactura> pagosList = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();

        for (PagoRequestDTO pagoReq : requestDTO.getPagos()) {
            if (pagoReq.getMonto() == null || pagoReq.getMonto() <= 0.0) {
                throw new PagoInvalidoException("Cada pago registrado debe tener un monto mayor a cero");
            }

            MetodoPago metodoPago = metodoPagoRepository.findById(pagoReq.getIdMetodoPago())
                    .orElseThrow(() -> new MetodoPagoNotFoundException(pagoReq.getIdMetodoPago()));

            if (!Boolean.TRUE.equals(metodoPago.getActivo())) {
                throw new PagoInvalidoException(String.format(
                        "El método de pago '%s' (código: %s) no se encuentra activo para procesar transacciones",
                        metodoPago.getNombre(), metodoPago.getCodigo()));
            }

            sumaPagos += pagoReq.getMonto();

            PagoFactura pagoFactura = PagoFactura.builder()
                    .idMetodoPago(metodoPago.getIdMetodoPago())
                    .nombreMetodoPago(metodoPago.getNombre())
                    .monto(pagoReq.getMonto())
                    .referenciaTransaccion(pagoReq.getReferenciaTransaccion())
                    .fechaHora(ahora)
                    .build();

            pagosList.add(pagoFactura);
        }

        sumaPagos = Math.round(sumaPagos * 100.0) / 100.0;

        if (Math.abs(sumaPagos - totalCalculado) > 0.01) {
            throw new PagoInvalidoException(String.format(
                    "Descuadre de caja: la suma de los pagos recibidos (%.2f) no coincide con el total calculado de la factura (%.2f)",
                    sumaPagos, totalCalculado));
        }

        String numeroFactura = facturaRepository.generarSiguienteNumeroFactura();

        Factura factura = Factura.builder()
                .numeroFactura(numeroFactura)
                .idPedido(pedido.getIdPedido())
                .idCliente(cliente.getIdCliente())
                .nombreCliente(cliente.getNombre())
                .identificacionCliente(cliente.getTelefono())
                .idUsuario(usuario.getIdUsuario())
                .nombreUsuario(usuario.getNombre())
                .fechaEmision(ahora)
                .subtotal(subtotal)
                .propina(propina)
                .descuento(descuento)
                .impuesto(impuesto)
                .total(totalCalculado)
                .estado(EstadoFactura.PAGADA.name())
                .build();

        Factura facturaGuardada = facturaRepository.save(factura);

        for (PagoFactura pago : pagosList) {
            pago.setIdFactura(facturaGuardada.getIdFactura());
        }

        pagoFacturaRepository.saveBatch(pagosList);
        facturaGuardada.setPagos(pagosList);

        pedidoRepository.updateEstado(pedido.getIdPedido(), EstadoPedido.PAGADO.name());

        if (pedido.getIdMesa() != null) {
            Optional<Mesa> mesaOpt = mesaRepository.findById(pedido.getIdMesa());
            if (mesaOpt.isPresent()) {
                Mesa mesa = mesaOpt.get();
                mesa.setEstado("DISPONIBLE");
                mesaRepository.update(mesa);
            }
        }

        FacturaResponseDTO responseDTO = facturaMapper.toResponseDTO(facturaGuardada);

        if (cliente.getEmail() != null && !cliente.getEmail().isBlank()) {
            emailService.sendFacturaEmail(cliente.getEmail(), responseDTO);
        }

        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponseDTO buscarPorId(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new FacturaNotFoundException(id));
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
                        .filter(f -> f.getFechaEmision() != null
                                && !f.getFechaEmision().isBefore(fechaInicio)
                                && !f.getFechaEmision().isAfter(fechaFin))
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
    @Transactional
    public FacturaResponseDTO anularFactura(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new FacturaNotFoundException(id));

        if (EstadoFactura.ANULADA.name().equalsIgnoreCase(factura.getEstado())) {
            throw new IllegalStateException(
                    String.format("La factura con ID %d (%s) ya se encuentra en estado ANULADA",
                            id, factura.getNumeroFactura()));
        }

        facturaRepository.updateEstado(id, EstadoFactura.ANULADA.name());
        factura.setEstado(EstadoFactura.ANULADA.name());

        return facturaMapper.toResponseDTO(factura);
    }

}
