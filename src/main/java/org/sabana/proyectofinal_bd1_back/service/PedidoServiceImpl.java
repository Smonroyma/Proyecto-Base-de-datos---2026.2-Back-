package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.DetallePedidoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PedidoMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.PedidoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PedidoResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;
import org.sabana.proyectofinal_bd1_back.model.entity.DetallePedido;
import org.sabana.proyectofinal_bd1_back.model.entity.EstadoPedido;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de la lógica de negocio para la gestión transaccional de Pedidos y Detalles.
 * Regla V2:
 * - Todo pedido exige obligatoriamente una mesa (numeroMesa) y un cliente (idCliente).
 * - Se maneja subtotal (antes de IVA 19% y propina).
 * - Máquina de estados unidireccional estricta:
 *   PENDIENTE -> EN_PREPARACION | CANCELADO
 *   EN_PREPARACION -> ENTREGADO
 *   ENTREGADO -> PAGADO
 */
@Service
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ClienteRepository clienteRepository;
    private final MesaRepository mesaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PedidoMapper pedidoMapper;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                             DetallePedidoRepository detallePedidoRepository,
                             ClienteRepository clienteRepository,
                             MesaRepository mesaRepository,
                             UsuarioRepository usuarioRepository,
                             ProductoRepository productoRepository,
                             PedidoMapper pedidoMapper) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.clienteRepository = clienteRepository;
        this.mesaRepository = mesaRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.pedidoMapper = pedidoMapper;
    }

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(PedidoRequestDTO requestDTO) {
        if (requestDTO.getNumeroMesa() == null) {
            throw new IllegalArgumentException("El número de mesa es obligatorio. No se permiten pedidos para llevar.");
        }

        Cliente cliente = clienteRepository.findById(requestDTO.getIdCliente())
                .orElseThrow(() -> new ClienteNotFoundException(requestDTO.getIdCliente()));

        Staff staff = usuarioRepository.findById(requestDTO.getIdUsuario())
                .orElseThrow(() -> new UsuarioNotFoundException(requestDTO.getIdUsuario()));

        Mesa mesa = mesaRepository.findByNumeroMesa(requestDTO.getNumeroMesa())
                .orElseThrow(() -> new MesaNotFoundException(requestDTO.getNumeroMesa()));

        if ("EN_MANTENIMIENTO".equalsIgnoreCase(mesa.getEstado())) {
            throw new IllegalStateException(String.format(
                    "La mesa número %d se encuentra en mantenimiento y no puede recibir pedidos", mesa.getNumeroMesa()));
        }

        if ("DISPONIBLE".equalsIgnoreCase(mesa.getEstado()) || "RESERVADA".equalsIgnoreCase(mesa.getEstado())) {
            mesa.setEstado("OCUPADA");
            mesaRepository.update(mesa);
        }

        if (requestDTO.getDetalles() == null || requestDTO.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un detalle de producto");
        }

        double subtotalPedido = 0.0;
        List<DetallePedido> detallesList = new ArrayList<>();

        for (DetallePedidoRequestDTO itemReq : requestDTO.getDetalles()) {
            Producto producto = productoRepository.findById(itemReq.getIdProducto())
                    .orElseThrow(() -> new ProductoNotFoundException(itemReq.getIdProducto()));

            if (!Boolean.TRUE.equals(producto.getDisponibilidad())) {
                throw new StockInsuficienteException(String.format(
                        "El producto '%s' no se encuentra disponible para la venta", producto.getNombre()));
            }

            if (producto.getStock() == null || producto.getStock() < itemReq.getCantidad()) {
                throw new StockInsuficienteException(String.format(
                        "Stock insuficiente para el producto '%s'. Stock actual: %d, solicitado: %d",
                        producto.getNombre(), producto.getStock() != null ? producto.getStock() : 0, itemReq.getCantidad()));
            }

            double precioUnitario = producto.getPrecio();
            double subtotalItem = Math.round(precioUnitario * itemReq.getCantidad() * 100.0) / 100.0;
            subtotalPedido += subtotalItem;

            int nuevoStock = producto.getStock() - itemReq.getCantidad();
            productoRepository.updateStock(producto.getIdProducto(), nuevoStock);
            producto.setStock(nuevoStock);

            DetallePedido detalle = DetallePedido.builder()
                    .idProducto(producto.getIdProducto())
                    .nombreProducto(producto.getNombre())
                    .cantidad(itemReq.getCantidad())
                    .precioUnitario(precioUnitario)
                    .subtotal(subtotalItem)
                    .build();

            detallesList.add(detalle);
        }

        subtotalPedido = Math.round(subtotalPedido * 100.0) / 100.0;

        Pedido pedido = Pedido.builder()
                .idCliente(cliente.getIdCliente())
                .nombreCliente(cliente.getNombreCompleto())
                .numeroMesa(mesa.getNumeroMesa())
                .idUsuario(staff.getIdUsuario())
                .nombreUsuario(staff.getNombreCompleto())
                .fecha(LocalDateTime.now())
                .estado(EstadoPedido.PENDIENTE.name())
                .subtotal(subtotalPedido)
                .observaciones(requestDTO.getObservaciones())
                .build();

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        for (DetallePedido d : detallesList) {
            d.setIdPedido(pedidoGuardado.getIdPedido());
        }

        detallePedidoRepository.saveBatch(detallesList);
        pedidoGuardado.setDetalles(detallesList);

        return pedidoMapper.toResponseDTO(pedidoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException(id));
        return pedidoMapper.toResponseDTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> obtenerPedidos(Long clienteId, String estado) {
        List<Pedido> pedidos;

        if (clienteId != null) {
            pedidos = pedidoRepository.findByCliente(clienteId);
        } else if (estado != null && !estado.isBlank()) {
            pedidos = pedidoRepository.findByEstado(estado.trim());
        } else {
            pedidos = pedidoRepository.findAll();
        }

        if (clienteId != null && estado != null && !estado.isBlank()) {
            pedidos = pedidos.stream()
                    .filter(p -> p.getEstado() != null && p.getEstado().equalsIgnoreCase(estado.trim()))
                    .toList();
        }

        return pedidoMapper.toResponseDTOList(pedidos);
    }

    @Override
    @Transactional
    public PedidoResponseDTO actualizarEstado(Long id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException(id));

        if (!EstadoPedido.esValido(nuevoEstado)) {
            throw new IllegalArgumentException(String.format(
                    "Estado de pedido inválido: '%s'. Estados permitidos: PENDIENTE, EN_PREPARACION, ENTREGADO, CANCELADO, PAGADO",
                    nuevoEstado));
        }

        String estadoActual = pedido.getEstado() != null ? pedido.getEstado().trim().toUpperCase() : "PENDIENTE";
        String estadoDestino = nuevoEstado.trim().toUpperCase();

        validarTransicionUnidireccional(estadoActual, estadoDestino);

        pedidoRepository.updateEstado(id, estadoDestino);
        pedido.setEstado(estadoDestino);

        if (EstadoPedido.CANCELADO.name().equals(estadoDestino) && pedido.getNumeroMesa() != null) {
            mesaRepository.updateEstado(pedido.getNumeroMesa(), "DISPONIBLE");
        }

        return pedidoMapper.toResponseDTO(pedido);
    }

    private void validarTransicionUnidireccional(String estadoActual, String estadoDestino) {
        if (estadoActual.equals(estadoDestino)) {
            throw new IllegalArgumentException(String.format(
                    "El pedido ya se encuentra en el estado '%s'", estadoActual));
        }

        boolean transicionValida = switch (estadoActual) {
            case "PENDIENTE" -> "EN_PREPARACION".equals(estadoDestino) || "CANCELADO".equals(estadoDestino);
            case "EN_PREPARACION" -> "ENTREGADO".equals(estadoDestino);
            case "ENTREGADO" -> "PAGADO".equals(estadoDestino);
            default -> false;
        };

        if (!transicionValida) {
            throw new IllegalArgumentException(String.format(
                    "Transición de estado no permitida: no se puede pasar de '%s' a '%s'. El flujo es estrictamente unidireccional (PENDIENTE -> EN_PREPARACION -> ENTREGADO -> PAGADO).",
                    estadoActual, estadoDestino));
        }
    }

    @Override
    @Transactional
    public void eliminarPedido(Long id) {
        if (pedidoRepository.findById(id).isEmpty()) {
            throw new PedidoNotFoundException(id);
        }
        pedidoRepository.deleteById(id);
    }

}
