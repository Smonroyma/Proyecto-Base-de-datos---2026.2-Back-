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
    public MetodoPagoResponseDTO buscarPorId(Long id) {
        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new MetodoPagoNotFoundException(id));
        return metodoPagoMapper.toResponseDTO(metodoPago);
    }

    @Override
    public MetodoPagoResponseDTO buscarPorCodigo(String codigo) {
        MetodoPago metodoPago = metodoPagoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new MetodoPagoNotFoundException(
                        String.format("No se encontró el método de pago con código: '%s'", codigo)));
        return metodoPagoMapper.toResponseDTO(metodoPago);
    }

    @Override
    public MetodoPagoResponseDTO crear(MetodoPagoRequestDTO requestDTO) {
        String codigoNormalizado = requestDTO.getCodigo().trim().toUpperCase();
        String nombreNormalizado = requestDTO.getNombre().trim();

        if (metodoPagoRepository.existsByCodigo(codigoNormalizado)) {
            throw new DuplicateResourceException(
                    String.format("Ya existe un método de pago registrado con el código: '%s'", codigoNormalizado));
        }

        MetodoPago metodoPago = metodoPagoMapper.toEntity(requestDTO);
        metodoPago.setCodigo(codigoNormalizado);
        metodoPago.setNombre(nombreNormalizado);
        if (metodoPago.getDescripcion() != null) {
            metodoPago.setDescripcion(metodoPago.getDescripcion().trim());
        }

        MetodoPago guardado = metodoPagoRepository.save(metodoPago);
        return metodoPagoMapper.toResponseDTO(guardado);
    }

    @Override
    public MetodoPagoResponseDTO actualizar(Long id, MetodoPagoRequestDTO requestDTO) {
        MetodoPago existente = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new MetodoPagoNotFoundException(id));

        String codigoNormalizado = requestDTO.getCodigo().trim().toUpperCase();
        String nombreNormalizado = requestDTO.getNombre().trim();

        if (metodoPagoRepository.existsByCodigoAndNotId(codigoNormalizado, id)) {
            throw new DuplicateResourceException(
                    String.format("Ya existe otro método de pago registrado con el código: '%s'", codigoNormalizado));
        }

        metodoPagoMapper.updateEntityFromDTO(requestDTO, existente);
        existente.setIdMetodoPago(id);
        existente.setCodigo(codigoNormalizado);
        existente.setNombre(nombreNormalizado);
        if (existente.getDescripcion() != null) {
            existente.setDescripcion(existente.getDescripcion().trim());
        }

        MetodoPago actualizado = metodoPagoRepository.update(existente);
        return metodoPagoMapper.toResponseDTO(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (metodoPagoRepository.findById(id).isEmpty()) {
            throw new MetodoPagoNotFoundException(id);
        }
        metodoPagoRepository.deleteById(id);
    }

}
