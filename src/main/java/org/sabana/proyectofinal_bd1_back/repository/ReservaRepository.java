package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;
import org.sabana.proyectofinal_bd1_back.model.entity.Reserva;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para la entidad Reserva.
 * Todas las operaciones se realizan exclusivamente mediante SQL nativo.
 */
public interface ReservaRepository {

    Optional<Reserva> findById(Long id);

    List<Reserva> findAll();

    List<Reserva> findByCliente(Long idCliente);

    List<Reserva> findByMesa(Long idMesa);

    List<Reserva> findByEstado(String estado);

    List<Mesa> findMesasDisponibles(LocalDateTime fechaHora, Integer capacidadRequerida);

    Reserva save(Reserva reserva);

    Reserva update(Reserva reserva);

    boolean deleteById(Long id);

    boolean existsOverlappingReserva(Long idMesa, LocalDateTime fechaHora, Long excludeReservaId);

}
