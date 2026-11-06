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

@Service
public class MesaServiceImpl implements MesaService {

    private final MesaRepository mesaRepository;
    private final MesaMapper mesaMapper;

    public MesaServiceImpl(MesaRepository mesaRepository, MesaMapper mesaMapper) {
        this.mesaRepository = mesaRepository;
        this.mesaMapper = mesaMapper;
    }

    @Override
    public MesaResponseDTO buscarPorNumero(Integer numeroMesa) {
        Mesa mesa = mesaRepository.findByNumeroMesa(numeroMesa)
                .orElseThrow(() -> new MesaNotFoundException(numeroMesa));
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
    public MesaResponseDTO crear(MesaRequestDTO requestDTO) {
        validarEstado(requestDTO.getEstado());

        if (mesaRepository.existsByNumeroMesa(requestDTO.getNumeroMesa())) {
            throw new MesaAlreadyExistsException(
                    String.format("Ya existe una mesa registrada con el número: %d", requestDTO.getNumeroMesa())
            );
        }

        Mesa mesa = mesaMapper.toEntity(requestDTO);
        mesa.setEstado(requestDTO.getEstado().toUpperCase().trim());

        Mesa mesaGuardada = mesaRepository.save(mesa);
        return mesaMapper.toResponseDTO(mesaGuardada);
    }

    @Override
    public MesaResponseDTO actualizar(Integer numeroMesa, MesaRequestDTO requestDTO) {
        Mesa mesaExistente = mesaRepository.findByNumeroMesa(numeroMesa)
                .orElseThrow(() -> new MesaNotFoundException(numeroMesa));

        validarEstado(requestDTO.getEstado());

        mesaExistente.setCapacidad(requestDTO.getCapacidad());
        mesaExistente.setEstado(requestDTO.getEstado().toUpperCase().trim());
        mesaExistente.setNumeroMesa(numeroMesa);

        Mesa mesaActualizada = mesaRepository.update(mesaExistente);
        return mesaMapper.toResponseDTO(mesaActualizada);
    }

    @Override
    public MesaResponseDTO actualizarEstado(Integer numeroMesa, String nuevoEstado) {
        Mesa mesaExistente = mesaRepository.findByNumeroMesa(numeroMesa)
                .orElseThrow(() -> new MesaNotFoundException(numeroMesa));

        validarEstado(nuevoEstado);
        String estadoNormalizado = nuevoEstado.toUpperCase().trim();
        mesaRepository.updateEstado(numeroMesa, estadoNormalizado);
        mesaExistente.setEstado(estadoNormalizado);
        return mesaMapper.toResponseDTO(mesaExistente);
    }

    @Override
    public void eliminar(Integer numeroMesa) {
        if (mesaRepository.findByNumeroMesa(numeroMesa).isEmpty()) {
            throw new MesaNotFoundException(numeroMesa);
        }
        mesaRepository.deleteByNumeroMesa(numeroMesa);
    }

    private void validarEstado(String estado) {
        if (!EstadoMesa.esValido(estado)) {
            throw new IllegalArgumentException(
                    String.format("El estado '%s' no es válido. Los estados permitidos son: DISPONIBLE, OCUPADA, RESERVADA, EN_MANTENIMIENTO", estado)
            );
        }
    }

}
