package org.sabana.proyectofinal_bd1_back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sabana.proyectofinal_bd1_back.model.dto.ClienteMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.ClienteRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ClienteResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;
import org.sabana.proyectofinal_bd1_back.repository.ClienteRepository;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ClienteAlreadyExistsException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ClienteNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente cliente;
    private ClienteRequestDTO requestDTO;
    private ClienteResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        cliente = Cliente.builder()
                .idCliente(1L)
                .nombre("Laura Gómez")
                .telefono("+57 310 9876543")
                .email("laura.gomez@savana.com")
                .fechaRegistro(now)
                .build();

        requestDTO = ClienteRequestDTO.builder()
                .nombre("Laura Gómez")
                .telefono("+57 310 9876543")
                .email("laura.gomez@savana.com")
                .build();

        responseDTO = ClienteResponseDTO.builder()
                .idCliente(1L)
                .nombre("Laura Gómez")
                .telefono("+57 310 9876543")
                .email("laura.gomez@savana.com")
                .fechaRegistro(now)
                .build();
    }

    @Test
    @DisplayName("buscarPorId retorna ClienteResponseDTO cuando el cliente existe")
    void buscarPorId_Exitoso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(responseDTO);

        ClienteResponseDTO result = clienteService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdCliente());
        assertEquals("Laura Gómez", result.getNombre());
        verify(clienteRepository).findById(1L);
    }

    @Test
    @DisplayName("buscarPorId lanza ClienteNotFoundException cuando el cliente no existe")
    void buscarPorId_NoExiste_LanzaClienteNotFoundException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.buscarPorId(99L));
        verify(clienteRepository).findById(99L);
    }

    @Test
    @DisplayName("obtenerTodos retorna lista de clientes")
    void obtenerTodos_Exitoso() {
        List<Cliente> clientes = List.of(cliente);
        List<ClienteResponseDTO> responseList = List.of(responseDTO);

        when(clienteRepository.findAll()).thenReturn(clientes);
        when(clienteMapper.toResponseDTOList(clientes)).thenReturn(responseList);

        List<ClienteResponseDTO> result = clienteService.obtenerTodos();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("crear registra un cliente exitosamente")
    void crear_Exitoso() {
        when(clienteRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(clienteRepository.existsByTelefono(requestDTO.getTelefono())).thenReturn(false);
        when(clienteMapper.toEntity(requestDTO)).thenReturn(cliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(responseDTO);

        ClienteResponseDTO result = clienteService.crear(requestDTO);

        assertNotNull(result);
        assertEquals("Laura Gómez", result.getNombre());
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("crear lanza ClienteAlreadyExistsException cuando el email ya existe")
    void crear_EmailDuplicado_LanzaClienteAlreadyExistsException() {
        when(clienteRepository.existsByEmail(requestDTO.getEmail())).thenReturn(true);

        assertThrows(ClienteAlreadyExistsException.class, () -> clienteService.crear(requestDTO));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("crear lanza ClienteAlreadyExistsException cuando el teléfono ya existe")
    void crear_TelefonoDuplicado_LanzaClienteAlreadyExistsException() {
        when(clienteRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(clienteRepository.existsByTelefono(requestDTO.getTelefono())).thenReturn(true);

        assertThrows(ClienteAlreadyExistsException.class, () -> clienteService.crear(requestDTO));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("actualizar modifica datos exitosamente cuando no hay colisiones")
    void actualizar_Exitoso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmailAndNotId(requestDTO.getEmail(), 1L)).thenReturn(false);
        when(clienteRepository.existsByTelefonoAndNotId(requestDTO.getTelefono(), 1L)).thenReturn(false);
        when(clienteRepository.update(cliente)).thenReturn(cliente);
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(responseDTO);

        ClienteResponseDTO result = clienteService.actualizar(1L, requestDTO);

        assertNotNull(result);
        verify(clienteRepository).update(cliente);
    }

    @Test
    @DisplayName("actualizar lanza ClienteNotFoundException cuando el ID no existe")
    void actualizar_NoExiste_LanzaClienteNotFoundException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.actualizar(99L, requestDTO));
        verify(clienteRepository, never()).update(any(Cliente.class));
    }

    @Test
    @DisplayName("actualizar lanza ClienteAlreadyExistsException cuando otro cliente tiene el mismo email")
    void actualizar_EmailDuplicado_LanzaClienteAlreadyExistsException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmailAndNotId(requestDTO.getEmail(), 1L)).thenReturn(true);

        assertThrows(ClienteAlreadyExistsException.class, () -> clienteService.actualizar(1L, requestDTO));
        verify(clienteRepository, never()).update(any(Cliente.class));
    }

    @Test
    @DisplayName("actualizar lanza ClienteAlreadyExistsException cuando otro cliente tiene el mismo teléfono")
    void actualizar_TelefonoDuplicado_LanzaClienteAlreadyExistsException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmailAndNotId(requestDTO.getEmail(), 1L)).thenReturn(false);
        when(clienteRepository.existsByTelefonoAndNotId(requestDTO.getTelefono(), 1L)).thenReturn(true);

        assertThrows(ClienteAlreadyExistsException.class, () -> clienteService.actualizar(1L, requestDTO));
        verify(clienteRepository, never()).update(any(Cliente.class));
    }

    @Test
    @DisplayName("eliminar borra el cliente cuando existe")
    void eliminar_Exitoso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        clienteService.eliminar(1L);

        verify(clienteRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar lanza ClienteNotFoundException cuando el cliente no existe")
    void eliminar_NoExiste_LanzaClienteNotFoundException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.eliminar(99L));
        verify(clienteRepository, never()).deleteById(99L);
    }

}
