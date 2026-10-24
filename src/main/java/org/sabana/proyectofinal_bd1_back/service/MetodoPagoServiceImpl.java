package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.MetodoPago;
import org.sabana.proyectofinal_bd1_back.repository.MetodoPagoRepository;
import org.sabana.proyectofinal_bd1_back.util.exceptions.DuplicateResourceException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.MetodoPagoNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de la lógica de negocio para la administración de métodos de pago.
 */
@Service
public class MetodoPagoServiceImpl implements MetodoPagoService {

    private final MetodoPagoRepository metodoPagoRepository;
    private final MetodoPagoMapper metodoPagoMapper;

    public MetodoPagoServiceImpl(MetodoPagoRepository metodoPagoRepository, MetodoPagoMapper metodoPagoMapper) {
        this.metodoPagoRepository = metodoPagoRepository;
        this.metodoPagoMapper = metodoPagoMapper;
    }

    @Override
    public List<MetodoPagoResponseDTO> obtenerMetodosPago(Boolean soloActivos) {
        List<MetodoPago> metodos;
        if (Boolean.TRUE.equals(soloActivos)) {
            metodos = metodoPagoRepository.findAllActivos();
        } else {
            metodos = metodoPagoRepository.findAll();
        }
        return metodoPagoMapper.toResponseDTOList(metodos);
    }

    @Override
    public MetodoPagoResponseDTO buscarPorTipo(String tipo) {
        MetodoPago metodoPago = metodoPagoRepository.findByTipo(tipo)
                .orElseThrow(() -> new MetodoPagoNotFoundException(
                        String.format("No se encontró el método de pago de tipo: '%s'", tipo)));
        return metodoPagoMapper.toResponseDTO(metodoPago);
    }

    @Override
    public MetodoPagoResponseDTO crear(MetodoPagoRequestDTO requestDTO) {
        String tipoNormalizado = requestDTO.getTipo().trim().toUpperCase();

        if (metodoPagoRepository.existsByTipo(tipoNormalizado)) {
            throw new DuplicateResourceException(
                    String.format("Ya existe un método de pago registrado con el tipo: '%s'", tipoNormalizado));
        }

        MetodoPago metodoPago = metodoPagoMapper.toEntity(requestDTO);
        metodoPago.setTipo(tipoNormalizado);

        MetodoPago guardado = metodoPagoRepository.save(metodoPago);
        return metodoPagoMapper.toResponseDTO(guardado);
    }

    @Override
    public MetodoPagoResponseDTO actualizar(String tipo, MetodoPagoRequestDTO requestDTO) {
        MetodoPago existente = metodoPagoRepository.findByTipo(tipo)
                .orElseThrow(() -> new MetodoPagoNotFoundException(
                        String.format("No se encontró el método de pago de tipo: '%s'", tipo)));

        String nuevoTipo = requestDTO.getTipo().trim().toUpperCase();

        if (!existente.getTipo().equalsIgnoreCase(nuevoTipo) && metodoPagoRepository.existsByTipo(nuevoTipo)) {
            throw new DuplicateResourceException(
                    String.format("Ya existe otro método de pago registrado con el tipo: '%s'", nuevoTipo));
        }

        String tipoOriginal = existente.getTipo();
        metodoPagoMapper.updateEntityFromDTO(requestDTO, existente);
        existente.setTipo(nuevoTipo);

        MetodoPago actualizado = metodoPagoRepository.update(tipoOriginal, existente);
        return metodoPagoMapper.toResponseDTO(actualizado);
    }

    @Override
    public void eliminar(String tipo) {
        if (metodoPagoRepository.findByTipo(tipo).isEmpty()) {
            throw new MetodoPagoNotFoundException(
                    String.format("No se encontró el método de pago de tipo: '%s'", tipo));
        }
        metodoPagoRepository.deleteByTipo(tipo);
    }

}
