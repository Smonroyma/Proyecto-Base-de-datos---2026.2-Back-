package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.MesaMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.MesaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.MesaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.EstadoMesa;
import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;
import org.sabana.proyectofinal_bd1_back.repository.MesaRepository;
import org.sabana.proyectofinal_bd1_back.util.exceptions.MesaAlreadyExistsException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.MesaNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de la lógica de negocio para la gestión de mesas.
 */
@Service
public class MesaServiceImpl implements MesaService {

    private final MesaRepository mesaRepository;
    private final MesaMapper mesaMapper;

    public MesaServiceImpl(MesaRepository mesaRepository, MesaMapper mesaMapper) {
        this.mesaRepository = mesaRepository;
        this.mesaMapper = mesaMapper;
    }

    @Override
    public MesaResponseDTO buscarPorId(Long id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new MesaNotFoundException(id));
        return mesaMapper.toResponseDTO(mesa);
    }

    @Override
    public List<MesaResponseDTO> obtenerMesas(String estado) {
        List<Mesa> mesas;
        if (estado != null && !estado.isBlank()) {
            validarEstado(estado);
            mesas = mesaRepository.findByEstado(estado.toUpperCase().trim());
        } else {
            mesas = mesaRepository.findAll();
        }
        return mesaMapper.toResponseDTOList(mesas);
    }

    @Override
    public MesaResponseDTO buscarPorNumero(Integer numero) {
        Mesa mesa = mesaRepository.findByNumero(numero)
                .orElseThrow(() -> new MesaNotFoundException(
                        String.format("No se encontró la mesa con el número: %d", numero)
                ));
        return mesaMapper.toResponseDTO(mesa);
    }

    @Override
    public MesaResponseDTO crear(MesaRequestDTO requestDTO) {
        validarEstado(requestDTO.getEstado());

        if (mesaRepository.existsByNumero(requestDTO.getNumero())) {
            throw new MesaAlreadyExistsException(
                    String.format("Ya existe una mesa registrada con el número: %d", requestDTO.getNumero())
            );
        }

        Mesa mesa = mesaMapper.toEntity(requestDTO);
        mesa.setEstado(requestDTO.getEstado().toUpperCase().trim());

        Mesa mesaGuardada = mesaRepository.save(mesa);
        return mesaMapper.toResponseDTO(mesaGuardada);
    }

    @Override
    public MesaResponseDTO actualizar(Long id, MesaRequestDTO requestDTO) {
        Mesa mesaExistente = mesaRepository.findById(id)
                .orElseThrow(() -> new MesaNotFoundException(id));

        validarEstado(requestDTO.getEstado());

        if (mesaRepository.existsByNumeroAndNotId(requestDTO.getNumero(), id)) {
            throw new MesaAlreadyExistsException(
                    String.format("Ya existe otra mesa registrada con el número: %d", requestDTO.getNumero())
            );
        }

        mesaMapper.updateEntityFromDTO(requestDTO, mesaExistente);
        mesaExistente.setIdMesa(id);
        mesaExistente.setEstado(requestDTO.getEstado().toUpperCase().trim());

        Mesa mesaActualizada = mesaRepository.update(mesaExistente);
        return mesaMapper.toResponseDTO(mesaActualizada);
    }

    @Override
    public void eliminar(Long id) {
        if (mesaRepository.findById(id).isEmpty()) {
            throw new MesaNotFoundException(id);
        }
        mesaRepository.deleteById(id);
    }

    private void validarEstado(String estado) {
        if (!EstadoMesa.esValido(estado)) {
            throw new IllegalArgumentException(
                    String.format("El estado '%s' no es válido. Los estados permitidos son: DISPONIBLE, OCUPADA, RESERVADA, EN_MANTENIMIENTO", estado)
            );
        }
    }

}
