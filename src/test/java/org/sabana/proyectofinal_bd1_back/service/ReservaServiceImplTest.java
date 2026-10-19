package org.sabana.proyectofinal_bd1_back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sabana.proyectofinal_bd1_back.model.dto.MesaMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.MesaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ReservaMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.ReservaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ReservaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private MesaRepository mesaRepository;

    @Mock
    private ReservaMapper reservaMapper;

    @Mock
    private MesaMapper mesaMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private Cliente cliente;
    private Mesa mesa;
    private Reserva reserva;
    private ReservaRequestDTO requestDTO;
    private ReservaResponseDTO responseDTO;
    private LocalDateTime fechaHora;

    @BeforeEach
    void setUp() {
        fechaHora = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0).withNano(0);

        cliente = Cliente.builder()
                .idCliente(1L)
                .nombres("Carlos")
                .apellidos("Mendoza")
                .telefono("3001234567")
                .correo("carlos@example.com")
                .fechaRegistro(LocalDateTime.now().minusDays(10))
                .build();

        mesa = Mesa.builder()
                .numeroMesa(5)
                .capacidad(4)
                .estado("DISPONIBLE")
                .build();

        reserva = Reserva.builder()
                .idReserva(10L)
                .idCliente(1L)
                .nombreCliente("Carlos Mendoza")
                .correoCliente("carlos@example.com")
                .numeroMesa(5)
                .fecha(fechaHora.toLocalDate())
                .hora(fechaHora.toLocalTime())
                .numeroPersonas(4)
                .estado("PENDIENTE")
                .build();

        requestDTO = ReservaRequestDTO.builder()
                .idCliente(1L)
                .numeroMesa(5)
                .fechaHora(fechaHora)
                .numeroPersonas(4)
                .estado("PENDIENTE")
                .build();

        responseDTO = ReservaResponseDTO.builder()
                .idReserva(10L)
                .idCliente(1L)
                .nombreCliente("Carlos Mendoza")
                .correoCliente("carlos@example.com")
                .numeroMesa(5)
                .fecha(fechaHora.toLocalDate())
                .hora(fechaHora.toLocalTime())
                .fechaHora(fechaHora)
                .numeroPersonas(4)
                .estado("PENDIENTE")
                .build();
    }

    @Test
    @DisplayName("crear registra exitosamente una reserva con idCliente existente y envía correo de confirmación")
    void crear_ConIdCliente_Exitoso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));
        when(reservaRepository.existsOverlappingReserva(5, fechaHora, null)).thenReturn(false);
        when(reservaMapper.toEntity(requestDTO)).thenReturn(reserva);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(responseDTO);

        ReservaResponseDTO result = reservaService.crear(requestDTO);

        assertNotNull(result);
        assertEquals(10L, result.getIdReserva());
        assertEquals(5, result.getNumeroMesa());
        verify(clienteRepository).findById(1L);
        verify(mesaRepository).findByNumeroMesa(5);
        verify(reservaRepository).save(any(Reserva.class));
        verify(emailService).sendNotificacionEmail(
                eq("carlos@example.com"),
                eq("Confirmación de Reserva"),
                eq("Carlos Mendoza"),
                anyString(),
                anyMap(),
                anyString()
        );
    }

    @Test
    @DisplayName("crear registra nuevo cliente permanentemente cuando correo no existe en BD")
    void crear_ClienteNuevoPorCorreo_GuardaPermanenteYCreaReserva() {
        ReservaRequestDTO reqConDatosCliente = ReservaRequestDTO.builder()
                .nombres("Carlos")
                .apellidos("Mendoza")
                .correo("carlos@example.com")
                .telefono("3001234567")
                .numeroMesa(5)
                .fecha(fechaHora.toLocalDate())
                .hora(fechaHora.toLocalTime())
                .numeroPersonas(4)
                .build();

        when(clienteRepository.findByCorreo("carlos@example.com")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));
        when(reservaRepository.existsOverlappingReserva(5, fechaHora, null)).thenReturn(false);
        when(reservaMapper.toEntity(reqConDatosCliente)).thenReturn(reserva);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(responseDTO);

        ReservaResponseDTO result = reservaService.crear(reqConDatosCliente);

        assertNotNull(result);
        verify(clienteRepository).save(any(Cliente.class));
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    @DisplayName("crear lanza ClienteNotFoundException cuando el idCliente no existe")
    void crear_ClienteNoExiste_LanzaClienteNotFoundException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> reservaService.crear(requestDTO));
        verify(mesaRepository, never()).findByNumeroMesa(any());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("crear lanza MesaNotFoundException cuando la mesa especificada no existe")
    void crear_MesaNoExiste_LanzaMesaNotFoundException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.empty());

        assertThrows(MesaNotFoundException.class, () -> reservaService.crear(requestDTO));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("crear lanza ReservaConflictException cuando la capacidad de la mesa es insuficiente")
    void crear_CapacidadInsuficiente_LanzaReservaConflictException() {
        mesa.setCapacidad(2);
        requestDTO.setNumeroPersonas(4);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));

        assertThrows(ReservaConflictException.class, () -> reservaService.crear(requestDTO));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("crear lanza ReservaConflictException cuando la mesa está en mantenimiento")
    void crear_MesaEnMantenimiento_LanzaReservaConflictException() {
        mesa.setEstado("EN_MANTENIMIENTO");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));

        assertThrows(ReservaConflictException.class, () -> reservaService.crear(requestDTO));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("crear lanza ReservaConflictException cuando hay solapamiento de horario en la mesa")
    void crear_SolapamientoHorario_LanzaReservaConflictException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));
        when(reservaRepository.existsOverlappingReserva(5, fechaHora, null)).thenReturn(true);

        assertThrows(ReservaConflictException.class, () -> reservaService.crear(requestDTO));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("buscarPorCorreoCliente retorna todas las reservas asociadas al correo único del cliente")
    void buscarPorCorreoCliente_Exitoso() {
        List<Reserva> lista = List.of(reserva);
        List<ReservaResponseDTO> dtoList = List.of(responseDTO);

        when(reservaRepository.findByClienteCorreo("carlos@example.com")).thenReturn(lista);
        when(reservaMapper.toResponseDTOList(lista)).thenReturn(dtoList);

        List<ReservaResponseDTO> result = reservaService.buscarPorCorreoCliente("carlos@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("carlos@example.com", result.getFirst().getCorreoCliente());
        verify(reservaRepository).findByClienteCorreo("carlos@example.com");
    }

    @Test
    @DisplayName("obtenerReservas con filtro por correo retorna reservas asociadas a ese correo")
    void obtenerReservas_FiltroCorreo_RetornaFiltradas() {
        List<Reserva> lista = List.of(reserva);
        List<ReservaResponseDTO> dtoList = List.of(responseDTO);

        when(reservaRepository.findByClienteCorreo("carlos@example.com")).thenReturn(lista);
        when(reservaMapper.toResponseDTOList(lista)).thenReturn(dtoList);

        List<ReservaResponseDTO> result = reservaService.obtenerReservas(null, "carlos@example.com", null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservaRepository).findByClienteCorreo("carlos@example.com");
    }

    @Test
    @DisplayName("buscarPorId retorna ReservaResponseDTO cuando la reserva existe")
    void buscarPorId_Exitoso() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(responseDTO);

        ReservaResponseDTO result = reservaService.buscarPorId(10L);

        assertNotNull(result);
        assertEquals(10L, result.getIdReserva());
        verify(reservaRepository).findById(10L);
    }

    @Test
    @DisplayName("buscarPorId lanza ReservaNotFoundException cuando la reserva no existe")
    void buscarPorId_NoExiste_LanzaReservaNotFoundException() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservaNotFoundException.class, () -> reservaService.buscarPorId(99L));
        verify(reservaRepository).findById(99L);
    }

    @Test
    @DisplayName("cambiarEstado a CANCELADA actualiza la reserva sin eliminar jamás al cliente")
    void cambiarEstado_Cancelada_EnviaCorreoSinEliminarCliente() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.update(reserva)).thenReturn(reserva);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(responseDTO);

        ReservaResponseDTO result = reservaService.cambiarEstado(10L, "CANCELADA");

        assertNotNull(result);
        assertEquals("CANCELADA", reserva.getEstado());
        verify(reservaRepository).update(reserva);
        verify(clienteRepository, never()).deleteById(any());
        verify(emailService).sendNotificacionEmail(
                eq("carlos@example.com"),
                eq("Cancelación de Reserva"),
                eq("Carlos Mendoza"),
                anyString(),
                anyMap(),
                anyString()
        );
    }

    @Test
    @DisplayName("consultarMesasDisponibles retorna las mesas libres para el horario y capacidad requeridos")
    void consultarMesasDisponibles_Exitoso() {
        MesaResponseDTO mesaDTO = MesaResponseDTO.builder()
                .numeroMesa(5)
                .capacidad(4)
                .estado("DISPONIBLE")
                .build();

        when(reservaRepository.findMesasDisponibles(fechaHora, 4)).thenReturn(List.of(mesa));
        when(mesaMapper.toResponseDTOList(List.of(mesa))).thenReturn(List.of(mesaDTO));

        List<MesaResponseDTO> result = reservaService.consultarMesasDisponibles(fechaHora, 4);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.getFirst().getNumeroMesa());
        verify(reservaRepository).findMesasDisponibles(fechaHora, 4);
    }

    @Test
    @DisplayName("consultarMesasDisponibles lanza IllegalArgumentException cuando la fecha es pasada")
    void consultarMesasDisponibles_FechaPasada_LanzaIllegalArgumentException() {
        LocalDateTime fechaPasada = LocalDateTime.now().minusHours(1);

        assertThrows(IllegalArgumentException.class,
                () -> reservaService.consultarMesasDisponibles(fechaPasada, 4));
        verify(reservaRepository, never()).findMesasDisponibles(any(), any());
    }

}
