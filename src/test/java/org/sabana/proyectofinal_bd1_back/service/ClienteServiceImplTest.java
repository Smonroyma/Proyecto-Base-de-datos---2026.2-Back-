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
                .nombres("Laura")
                .apellidos("Gómez")
                .telefono("+57 310 9876543")
                .correo("laura.gomez@savana.com")
                .fechaRegistro(now)
                .build();

        requestDTO = ClienteRequestDTO.builder()
                .nombres("Laura")
                .apellidos("Gómez")
                .telefono("+57 310 9876543")
                .correo("laura.gomez@savana.com")
                .build();

        responseDTO = ClienteResponseDTO.builder()
                .idCliente(1L)
                .nombres("Laura")
                .apellidos("Gómez")
                .telefono("+57 310 9876543")
                .correo("laura.gomez@savana.com")
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
        assertEquals("Laura", result.getNombres());
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
    @DisplayName("crear registra un cliente permanentemente de forma exitosa")
    void crear_Exitoso() {
        when(clienteRepository.existsByCorreo(requestDTO.getCorreo())).thenReturn(false);
        when(clienteRepository.existsByTelefono(requestDTO.getTelefono())).thenReturn(false);
        when(clienteMapper.toEntity(requestDTO)).thenReturn(cliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(responseDTO);

        ClienteResponseDTO result = clienteService.crear(requestDTO);

        assertNotNull(result);
        assertEquals("laura.gomez@savana.com", result.getCorreo());
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("crear lanza ClienteAlreadyExistsException cuando el correo ya existe")
    void crear_CorreoDuplicado_LanzaClienteAlreadyExistsException() {
        when(clienteRepository.existsByCorreo(requestDTO.getCorreo())).thenReturn(true);

        assertThrows(ClienteAlreadyExistsException.class, () -> clienteService.crear(requestDTO));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("crear lanza ClienteAlreadyExistsException cuando el teléfono ya existe")
    void crear_TelefonoDuplicado_LanzaClienteAlreadyExistsException() {
        when(clienteRepository.existsByCorreo(requestDTO.getCorreo())).thenReturn(false);
        when(clienteRepository.existsByTelefono(requestDTO.getTelefono())).thenReturn(true);

        assertThrows(ClienteAlreadyExistsException.class, () -> clienteService.crear(requestDTO));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("actualizar modifica datos exitosamente cuando no hay colisiones")
    void actualizar_Exitoso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCorreoAndNotId(requestDTO.getCorreo(), 1L)).thenReturn(false);
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
    @DisplayName("actualizar lanza ClienteAlreadyExistsException cuando otro cliente tiene el mismo correo")
    void actualizar_CorreoDuplicado_LanzaClienteAlreadyExistsException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCorreoAndNotId(requestDTO.getCorreo(), 1L)).thenReturn(true);

        assertThrows(ClienteAlreadyExistsException.class, () -> clienteService.actualizar(1L, requestDTO));
        verify(clienteRepository, never()).update(any(Cliente.class));
    }

    @Test
    @DisplayName("actualizar lanza ClienteAlreadyExistsException cuando otro cliente tiene el mismo teléfono")
    void actualizar_TelefonoDuplicado_LanzaClienteAlreadyExistsException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCorreoAndNotId(requestDTO.getCorreo(), 1L)).thenReturn(false);
        when(clienteRepository.existsByTelefonoAndNotId(requestDTO.getTelefono(), 1L)).thenReturn(true);

        assertThrows(ClienteAlreadyExistsException.class, () -> clienteService.actualizar(1L, requestDTO));
        verify(clienteRepository, never()).update(any(Cliente.class));
    }

    @Test
    @DisplayName("eliminar limpia reservas históricas y borra el cliente cuando no tiene reservas activas ni pedidos")
    void eliminar_Exitoso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.hasActiveReservas(1L)).thenReturn(false);
        when(clienteRepository.hasPedidosOrFacturas(1L)).thenReturn(false);

        clienteService.eliminar(1L);

        verify(clienteRepository).deleteHistoricalReservasByClienteId(1L);
        verify(clienteRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar lanza IllegalStateException cuando el cliente tiene reservas activas")
    void eliminar_ConReservasActivas_LanzaIllegalStateException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.hasActiveReservas(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> clienteService.eliminar(1L));
        verify(clienteRepository, never()).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar lanza IllegalStateException cuando el cliente tiene pedidos o facturas contables")
    void eliminar_ConPedidosOFacturas_LanzaIllegalStateException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.hasActiveReservas(1L)).thenReturn(false);
        when(clienteRepository.hasPedidosOrFacturas(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> clienteService.eliminar(1L));
        verify(clienteRepository, never()).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar lanza ClienteNotFoundException cuando el cliente no existe")
    void eliminar_NoExiste_LanzaClienteNotFoundException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.eliminar(99L));
        verify(clienteRepository, never()).deleteById(99L);
    }

}
