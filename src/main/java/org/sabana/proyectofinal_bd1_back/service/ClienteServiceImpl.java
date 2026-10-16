package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.ClienteMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.ClienteRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ClienteResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;
import org.sabana.proyectofinal_bd1_back.repository.ClienteRepository;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ClienteAlreadyExistsException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ClienteNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de la lógica de negocio para la gestión de clientes.
 */
@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteServiceImpl(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Override
    public ClienteResponseDTO buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));
        return clienteMapper.toResponseDTO(cliente);
    }

    @Override
    public List<ClienteResponseDTO> obtenerTodos() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clienteMapper.toResponseDTOList(clientes);
    }

    @Override
    public ClienteResponseDTO crear(ClienteRequestDTO requestDTO) {
        if (clienteRepository.existsByEmail(requestDTO.getEmail())) {
            throw new ClienteAlreadyExistsException(
                    String.format("Ya existe un cliente registrado con el correo: %s", requestDTO.getEmail())
            );
        }

        if (clienteRepository.existsByTelefono(requestDTO.getTelefono())) {
            throw new ClienteAlreadyExistsException(
                    String.format("Ya existe un cliente registrado con el teléfono: %s", requestDTO.getTelefono())
            );
        }

        Cliente cliente = clienteMapper.toEntity(requestDTO);
        cliente.setFechaRegistro(LocalDateTime.now());

        Cliente clienteGuardado = clienteRepository.save(cliente);
        return clienteMapper.toResponseDTO(clienteGuardado);
    }

    @Override
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO requestDTO) {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));

        if (clienteRepository.existsByEmailAndNotId(requestDTO.getEmail(), id)) {
            throw new ClienteAlreadyExistsException(
                    String.format("Ya existe otro cliente registrado con el correo: %s", requestDTO.getEmail())
            );
        }

        if (clienteRepository.existsByTelefonoAndNotId(requestDTO.getTelefono(), id)) {
            throw new ClienteAlreadyExistsException(
                    String.format("Ya existe otro cliente registrado con el teléfono: %s", requestDTO.getTelefono())
            );
        }

        clienteMapper.updateEntityFromDTO(requestDTO, clienteExistente);
        clienteExistente.setIdCliente(id);

        Cliente clienteActualizado = clienteRepository.update(clienteExistente);
        return clienteMapper.toResponseDTO(clienteActualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (clienteRepository.findById(id).isEmpty()) {
            throw new ClienteNotFoundException(id);
        }
        clienteRepository.deleteById(id);
    }

}
