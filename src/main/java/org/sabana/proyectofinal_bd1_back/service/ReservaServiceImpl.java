package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.MesaMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.MesaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ReservaMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.ReservaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ReservaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;
import org.sabana.proyectofinal_bd1_back.model.entity.EstadoReserva;
import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;
import org.sabana.proyectofinal_bd1_back.model.entity.Reserva;
import org.sabana.proyectofinal_bd1_back.repository.ClienteRepository;
import org.sabana.proyectofinal_bd1_back.repository.MesaRepository;
import org.sabana.proyectofinal_bd1_back.repository.ReservaRepository;
import org.sabana.proyectofinal_bd1_back.util.email.EmailService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ClienteNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.MesaNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ReservaConflictException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ReservaNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservaServiceImpl implements ReservaService {

    private static final DateTimeFormatter FECHA_HORA_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final MesaRepository mesaRepository;
    private final ReservaMapper reservaMapper;
    private final MesaMapper mesaMapper;
    private final EmailService emailService;

    public ReservaServiceImpl(ReservaRepository reservaRepository,
                              ClienteRepository clienteRepository,
                              MesaRepository mesaRepository,
                              ReservaMapper reservaMapper,
                              MesaMapper mesaMapper,
                              EmailService emailService) {
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.mesaRepository = mesaRepository;
        this.reservaMapper = reservaMapper;
        this.mesaMapper = mesaMapper;
        this.emailService = emailService;
    }

    @Override
    public List<ReservaResponseDTO> obtenerReservas(Long clienteId, String correo, Integer numeroMesa, String estado) {
        List<Reserva> reservas;

        if (correo != null && !correo.isBlank()) {
            reservas = reservaRepository.findByClienteCorreo(correo.trim());
        } else if (clienteId != null) {
            reservas = reservaRepository.findByCliente(clienteId);
        } else if (numeroMesa != null) {
            reservas = reservaRepository.findByMesa(numeroMesa);
        } else if (estado != null && !estado.isBlank()) {
            reservas = reservaRepository.findByEstado(estado.trim());
        } else {
            reservas = reservaRepository.findAll();
        }

        if (estado != null && !estado.isBlank() && (correo != null || clienteId != null || numeroMesa != null)) {
            reservas = reservas.stream()
                    .filter(r -> r.getEstado() != null && r.getEstado().equalsIgnoreCase(estado.trim()))
                    .toList();
        }

        return reservaMapper.toResponseDTOList(reservas);
    }

    @Override
    public List<ReservaResponseDTO> buscarPorCorreoCliente(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico del cliente es obligatorio para consultar sus reservas");
        }
        List<Reserva> reservas = reservaRepository.findByClienteCorreo(correo.trim());
        return reservaMapper.toResponseDTOList(reservas);
    }

    @Override
    public ReservaResponseDTO buscarPorId(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(id));
        return reservaMapper.toResponseDTO(reserva);
    }

    @Override
    @Transactional
    public ReservaResponseDTO crear(ReservaRequestDTO requestDTO) {
        LocalDateTime fechaHora = requestDTO.resolveFechaHora();
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha y hora de la reserva son obligatorias");
        }
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha y hora de la reserva debe ser futura");
        }

        Cliente cliente = resolverOPersistirCliente(requestDTO);

        Mesa mesa = mesaRepository.findByNumeroMesa(requestDTO.getNumeroMesa())
                .orElseThrow(() -> new MesaNotFoundException(requestDTO.getNumeroMesa()));

        if (mesa.getCapacidad() < requestDTO.getNumeroPersonas()) {
            throw new ReservaConflictException(String.format(
                    "La mesa número %d tiene una capacidad máxima de %d personas, insuficiente para %d personas.",
                    mesa.getNumeroMesa(), mesa.getCapacidad(), requestDTO.getNumeroPersonas()));
        }

        if ("EN_MANTENIMIENTO".equalsIgnoreCase(mesa.getEstado())) {
            throw new ReservaConflictException(String.format(
                    "La mesa número %d se encuentra en mantenimiento y no puede ser reservada.",
                    mesa.getNumeroMesa()));
        }

        if (reservaRepository.existsOverlappingReserva(requestDTO.getNumeroMesa(), fechaHora, null)) {
            throw new ReservaConflictException(String.format(
                    "La mesa número %d ya cuenta con una reserva activa en el intervalo de horario solicitado.",
                    mesa.getNumeroMesa()));
        }

        Reserva reserva = reservaMapper.toEntity(requestDTO);
        if (requestDTO.getIdReserva() != null) {
            reserva.setIdReserva(requestDTO.getIdReserva());
        }
        reserva.setIdCliente(cliente.getIdCliente());
        reserva.unirClaveCompuesta(reserva.getIdReserva(), cliente.getIdCliente());
        reserva.setNumeroMesa(mesa.getNumeroMesa());
        reserva.setFecha(fechaHora.toLocalDate());
        reserva.setHora(fechaHora.toLocalTime());
        if (reserva.getEstado() == null || reserva.getEstado().isBlank()) {
            reserva.setEstado(EstadoReserva.PENDIENTE.name());
        } else {
            reserva.setEstado(reserva.getEstado().toUpperCase().trim());
        }

        Reserva guardada = reservaRepository.save(reserva);
        guardada.unirClaveCompuesta(guardada.getIdReserva(), cliente.getIdCliente());
        guardada.setNombreCliente(cliente.getNombreCompleto());
        guardada.setCorreoCliente(cliente.getCorreo());
        guardada.setNumeroMesa(mesa.getNumeroMesa());

        if (cliente.getCorreo() != null && !cliente.getCorreo().isBlank()) {
            Map<String, String> detalles = new LinkedHashMap<>();
            detalles.put("Reserva No.", "#" + guardada.getIdReserva());
            detalles.put("Mesa Asignada", "Mesa " + mesa.getNumeroMesa());
            detalles.put("Fecha y Hora", fechaHora.format(FECHA_HORA_FORMATTER));
            detalles.put("Personas", String.valueOf(guardada.getNumeroPersonas()));
            detalles.put("Estado", guardada.getEstado());

            emailService.sendNotificacionEmail(
                    cliente.getCorreo(),
                    "Confirmación de Reserva",
                    cliente.getNombreCompleto(),
                    "Tu reserva en Savana Coffee ha sido registrada con éxito. A continuación encontrarás los detalles:",
                    detalles,
                    "Recuerda presentarte puntualmente. El cupo de la mesa se mantiene reservado hasta 15 minutos después de la hora acordada."
            );
        }

        return reservaMapper.toResponseDTO(guardada);
    }

    @Override
    @Transactional
    public ReservaResponseDTO actualizar(Long id, ReservaRequestDTO requestDTO) {
        Reserva reservaExistente = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(id));

        LocalDateTime fechaHora = requestDTO.resolveFechaHora();
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha y hora de la reserva son obligatorias");
        }

        Cliente cliente = resolverOPersistirCliente(requestDTO);

        Mesa mesa = mesaRepository.findByNumeroMesa(requestDTO.getNumeroMesa())
                .orElseThrow(() -> new MesaNotFoundException(requestDTO.getNumeroMesa()));

        if (mesa.getCapacidad() < requestDTO.getNumeroPersonas()) {
            throw new ReservaConflictException(String.format(
                    "La mesa número %d tiene una capacidad máxima de %d personas, insuficiente para %d personas.",
                    mesa.getNumeroMesa(), mesa.getCapacidad(), requestDTO.getNumeroPersonas()));
        }

        if (reservaRepository.existsOverlappingReserva(requestDTO.getNumeroMesa(), fechaHora, id)) {
            throw new ReservaConflictException(String.format(
                    "La mesa número %d ya cuenta con una reserva activa en el intervalo de horario solicitado.",
                    mesa.getNumeroMesa()));
        }

        reservaMapper.updateEntityFromDTO(requestDTO, reservaExistente);
        reservaExistente.unirClaveCompuesta(id, cliente.getIdCliente());
        reservaExistente.setNumeroMesa(mesa.getNumeroMesa());
        reservaExistente.setFecha(fechaHora.toLocalDate());
        reservaExistente.setHora(fechaHora.toLocalTime());
        if (reservaExistente.getEstado() != null) {
            reservaExistente.setEstado(reservaExistente.getEstado().toUpperCase().trim());
        }

        Reserva actualizada = reservaRepository.update(reservaExistente);
        actualizada.unirClaveCompuesta(actualizada.getIdReserva(), cliente.getIdCliente());
        actualizada.setNombreCliente(cliente.getNombreCompleto());
        actualizada.setCorreoCliente(cliente.getCorreo());
        actualizada.setNumeroMesa(mesa.getNumeroMesa());

        return reservaMapper.toResponseDTO(actualizada);
    }

    @Override
    @Transactional
    public ReservaResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        Reserva reservaExistente = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(id));

        if (!EstadoReserva.esValido(nuevoEstado)) {
            throw new IllegalArgumentException(String.format(
                    "Estado de reserva inválido: '%s'. Estados permitidos: PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA",
                    nuevoEstado));
        }

        String estadoNormalizado = nuevoEstado.trim().toUpperCase();
        reservaExistente.setEstado(estadoNormalizado);
        Reserva actualizada = reservaRepository.update(reservaExistente);

        if (EstadoReserva.CANCELADA.name().equals(estadoNormalizado)) {
            notificarCancelacionReserva(actualizada != null ? actualizada : reservaExistente);
        }

        return reservaMapper.toResponseDTO(actualizada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Reserva reservaExistente = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(id));

        reservaRepository.deleteById(id);
        notificarCancelacionReserva(reservaExistente);
    }

    @Override
    public List<MesaResponseDTO> consultarMesasDisponibles(LocalDateTime fechaHora, Integer numeroPersonas) {
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha y hora de consulta es obligatoria");
        }
        if (numeroPersonas == null || numeroPersonas < 1) {
            throw new IllegalArgumentException("El número de personas debe ser mayor o igual a 1");
        }
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha y hora de consulta debe ser futura");
        }

        List<Mesa> mesas = reservaRepository.findMesasDisponibles(fechaHora, numeroPersonas);
        return mesaMapper.toResponseDTOList(mesas);
    }

    private Cliente resolverOPersistirCliente(ReservaRequestDTO requestDTO) {
        if (requestDTO.getCorreo() != null && !requestDTO.getCorreo().isBlank()) {
            String correoLimpio = requestDTO.getCorreo().trim();
            return clienteRepository.findByCorreo(correoLimpio)
                    .orElseGet(() -> {
                        if (requestDTO.getNombre() == null || requestDTO.getNombre().isBlank()) {
                            throw new IllegalArgumentException("El nombre del cliente es obligatorio para registrar un nuevo cliente en la reserva");
                        }
                        if (requestDTO.getTelefono() == null || requestDTO.getTelefono().isBlank()) {
                            throw new IllegalArgumentException("El teléfono del cliente es obligatorio para registrar un nuevo cliente en la reserva");
                        }
                        Cliente nuevo = Cliente.builder()
                                .nombre(requestDTO.getNombre().trim())
                                .apellido(requestDTO.getApellido() != null ? requestDTO.getApellido().trim() : "")
                                .correo(correoLimpio)
                                .telefono(requestDTO.getTelefono().trim())
                                .build();
                        return clienteRepository.save(nuevo);
                    });
        }

        if (requestDTO.getIdCliente() != null) {
            return clienteRepository.findById(requestDTO.getIdCliente())
                    .orElseThrow(() -> new ClienteNotFoundException(requestDTO.getIdCliente()));
        }

        throw new IllegalArgumentException("Debe especificar el correo y datos del cliente o un idCliente válido");
    }

    private void notificarCancelacionReserva(Reserva reserva) {
        if (reserva == null || reserva.getIdCliente() == null) {
            return;
        }
        clienteRepository.findById(reserva.getIdCliente()).ifPresent(cliente -> {
            if (cliente.getCorreo() != null && !cliente.getCorreo().isBlank()) {
                Map<String, String> detalles = new LinkedHashMap<>();
                detalles.put("Reserva No.", "#" + reserva.getIdReserva());
                detalles.put("Mesa Liberada", "Mesa " + reserva.getNumeroMesa());
                detalles.put("Fecha y Hora Original", reserva.getFechaHora() != null
                        ? reserva.getFechaHora().format(FECHA_HORA_FORMATTER)
                        : "N/A");
                detalles.put("Personas", String.valueOf(reserva.getNumeroPersonas()));
                detalles.put("Estado", EstadoReserva.CANCELADA.name());

                emailService.sendNotificacionEmail(
                        cliente.getCorreo(),
                        "Cancelación de Reserva",
                        cliente.getNombreCompleto(),
                        "Te confirmamos que tu reserva en Savana Coffee ha sido cancelada y el cupo de la mesa ha sido liberado.",
                        detalles,
                        "Esperamos poder atenderte en una próxima ocasión. Puedes programar una nueva reserva cuando lo desees."
                );
            }
        });
    }

}
