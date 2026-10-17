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
import org.sabana.proyectofinal_bd1_back.util.exceptions.ClienteNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.MesaNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ReservaConflictException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ReservaNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de la lógica de negocio para la administración de reservas.
 */
@Service
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final MesaRepository mesaRepository;
    private final ReservaMapper reservaMapper;
    private final MesaMapper mesaMapper;

    public ReservaServiceImpl(ReservaRepository reservaRepository,
                              ClienteRepository clienteRepository,
                              MesaRepository mesaRepository,
                              ReservaMapper reservaMapper,
                              MesaMapper mesaMapper) {
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.mesaRepository = mesaRepository;
        this.reservaMapper = reservaMapper;
        this.mesaMapper = mesaMapper;
    }

    @Override
    public List<ReservaResponseDTO> obtenerReservas(Long clienteId, Long mesaId, String estado) {
        List<Reserva> reservas;

        if (clienteId != null) {
            reservas = reservaRepository.findByCliente(clienteId);
        } else if (mesaId != null) {
            reservas = reservaRepository.findByMesa(mesaId);
        } else if (estado != null && !estado.isBlank()) {
            reservas = reservaRepository.findByEstado(estado);
        } else {
            reservas = reservaRepository.findAll();
        }

        if (clienteId != null && estado != null && !estado.isBlank()) {
            reservas = reservas.stream()
                    .filter(r -> r.getEstado() != null && r.getEstado().equalsIgnoreCase(estado))
                    .toList();
        }
        if (mesaId != null && estado != null && !estado.isBlank()) {
            reservas = reservas.stream()
                    .filter(r -> r.getEstado() != null && r.getEstado().equalsIgnoreCase(estado))
                    .toList();
        }

        return reservaMapper.toResponseDTOList(reservas);
    }

    @Override
    public ReservaResponseDTO buscarPorId(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(id));
        return reservaMapper.toResponseDTO(reserva);
    }

    @Override
    public ReservaResponseDTO crear(ReservaRequestDTO requestDTO) {
        Cliente cliente = clienteRepository.findById(requestDTO.getIdCliente())
                .orElseThrow(() -> new ClienteNotFoundException(requestDTO.getIdCliente()));

        Mesa mesa = mesaRepository.findById(requestDTO.getIdMesa())
                .orElseThrow(() -> new MesaNotFoundException(requestDTO.getIdMesa()));

        if (mesa.getCapacidad() < requestDTO.getNumeroPersonas()) {
            throw new ReservaConflictException(String.format(
                    "La mesa número %d tiene una capacidad máxima de %d personas, insuficiente para %d personas.",
                    mesa.getNumero(), mesa.getCapacidad(), requestDTO.getNumeroPersonas()));
        }

        if ("EN_MANTENIMIENTO".equalsIgnoreCase(mesa.getEstado())) {
            throw new ReservaConflictException(String.format(
                    "La mesa número %d se encuentra en mantenimiento y no puede ser reservada.",
                    mesa.getNumero()));
        }

        if (reservaRepository.existsOverlappingReserva(requestDTO.getIdMesa(), requestDTO.getFechaHora(), null)) {
            throw new ReservaConflictException(String.format(
                    "La mesa número %d ya cuenta con una reserva activa en el intervalo de horario solicitado.",
                    mesa.getNumero()));
        }

        Reserva reserva = reservaMapper.toEntity(requestDTO);
        if (reserva.getEstado() == null || reserva.getEstado().isBlank()) {
            reserva.setEstado(EstadoReserva.PENDIENTE.name());
        } else {
            reserva.setEstado(reserva.getEstado().toUpperCase());
        }

        Reserva guardada = reservaRepository.save(reserva);
        guardada.setNombreCliente(cliente.getNombre());
        guardada.setNumeroMesa(mesa.getNumero());

        return reservaMapper.toResponseDTO(guardada);
    }

    @Override
    public ReservaResponseDTO actualizar(Long id, ReservaRequestDTO requestDTO) {
        Reserva reservaExistente = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(id));

        Cliente cliente = clienteRepository.findById(requestDTO.getIdCliente())
                .orElseThrow(() -> new ClienteNotFoundException(requestDTO.getIdCliente()));

        Mesa mesa = mesaRepository.findById(requestDTO.getIdMesa())
                .orElseThrow(() -> new MesaNotFoundException(requestDTO.getIdMesa()));

        if (mesa.getCapacidad() < requestDTO.getNumeroPersonas()) {
            throw new ReservaConflictException(String.format(
                    "La mesa número %d tiene una capacidad máxima de %d personas, insuficiente para %d personas.",
                    mesa.getNumero(), mesa.getCapacidad(), requestDTO.getNumeroPersonas()));
        }

        if (reservaRepository.existsOverlappingReserva(requestDTO.getIdMesa(), requestDTO.getFechaHora(), id)) {
            throw new ReservaConflictException(String.format(
                    "La mesa número %d ya cuenta con una reserva activa en el intervalo de horario solicitado.",
                    mesa.getNumero()));
        }

        reservaMapper.updateEntityFromDTO(requestDTO, reservaExistente);
        reservaExistente.setIdReserva(id);
        if (reservaExistente.getEstado() != null) {
            reservaExistente.setEstado(reservaExistente.getEstado().toUpperCase());
        }

        Reserva actualizada = reservaRepository.update(reservaExistente);
        actualizada.setNombreCliente(cliente.getNombre());
        actualizada.setNumeroMesa(mesa.getNumero());

        return reservaMapper.toResponseDTO(actualizada);
    }

    @Override
    public ReservaResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        Reserva reservaExistente = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(id));

        if (!EstadoReserva.esValido(nuevoEstado)) {
            throw new IllegalArgumentException(String.format(
                    "Estado de reserva inválido: '%s'. Estados permitidos: PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA",
                    nuevoEstado));
        }

        reservaExistente.setEstado(nuevoEstado.trim().toUpperCase());
        Reserva actualizada = reservaRepository.update(reservaExistente);
        return reservaMapper.toResponseDTO(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        if (reservaRepository.findById(id).isEmpty()) {
            throw new ReservaNotFoundException(id);
        }
        reservaRepository.deleteById(id);
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

}
