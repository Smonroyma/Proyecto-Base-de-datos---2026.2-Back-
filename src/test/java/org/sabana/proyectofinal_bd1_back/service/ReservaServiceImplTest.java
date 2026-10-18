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
                .nombre("Carlos Mendoza")
                .telefono("3001234567")
                .email("carlos@example.com")
                .fechaRegistro(LocalDateTime.now().minusDays(10))
                .build();

        mesa = Mesa.builder()
                .idMesa(2L)
                .numero(5)
                .capacidad(4)
                .estado("DISPONIBLE")
                .build();

        reserva = Reserva.builder()
                .idReserva(10L)
                .idCliente(1L)
                .nombreCliente("Carlos Mendoza")
                .idMesa(2L)
                .numeroMesa(5)
                .fechaHora(fechaHora)
                .numeroPersonas(4)
                .estado("PENDIENTE")
                .observaciones("Cena de aniversario")
                .build();

        requestDTO = ReservaRequestDTO.builder()
                .idCliente(1L)
                .idMesa(2L)
                .fechaHora(fechaHora)
                .numeroPersonas(4)
                .estado("PENDIENTE")
                .observaciones("Cena de aniversario")
                .build();

        responseDTO = ReservaResponseDTO.builder()
                .idReserva(10L)
                .idCliente(1L)
                .nombreCliente("Carlos Mendoza")
                .idMesa(2L)
                .numeroMesa(5)
                .fechaHora(fechaHora)
                .numeroPersonas(4)
                .estado("PENDIENTE")
                .observaciones("Cena de aniversario")
                .build();
    }

    @Test
    @DisplayName("crear registra exitosamente una reserva cuando cliente, mesa y horario son válidos y envía correo de confirmación")
    void crear_Exitoso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mesaRepository.findById(2L)).thenReturn(Optional.of(mesa));
        when(reservaRepository.existsOverlappingReserva(2L, fechaHora, null)).thenReturn(false);
        when(reservaMapper.toEntity(requestDTO)).thenReturn(reserva);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(responseDTO);

        ReservaResponseDTO result = reservaService.crear(requestDTO);

        assertNotNull(result);
        assertEquals(10L, result.getIdReserva());
        assertEquals("Carlos Mendoza", result.getNombreCliente());
        assertEquals(5, result.getNumeroMesa());
        verify(clienteRepository).findById(1L);
        verify(mesaRepository).findById(2L);
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
    @DisplayName("crear lanza ClienteNotFoundException cuando el cliente no existe")
    void crear_ClienteNoExiste_LanzaClienteNotFoundException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> reservaService.crear(requestDTO));
        verify(mesaRepository, never()).findById(any());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("crear lanza MesaNotFoundException cuando la mesa especificada no existe")
    void crear_MesaNoExiste_LanzaMesaNotFoundException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mesaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(MesaNotFoundException.class, () -> reservaService.crear(requestDTO));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("crear lanza ReservaConflictException cuando la capacidad de la mesa es insuficiente")
    void crear_CapacidadInsuficiente_LanzaReservaConflictException() {
        mesa.setCapacidad(2);
        requestDTO.setNumeroPersonas(4);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mesaRepository.findById(2L)).thenReturn(Optional.of(mesa));

        assertThrows(ReservaConflictException.class, () -> reservaService.crear(requestDTO));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("crear lanza ReservaConflictException cuando la mesa está en mantenimiento")
    void crear_MesaEnMantenimiento_LanzaReservaConflictException() {
        mesa.setEstado("EN_MANTENIMIENTO");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mesaRepository.findById(2L)).thenReturn(Optional.of(mesa));

        assertThrows(ReservaConflictException.class, () -> reservaService.crear(requestDTO));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("crear lanza ReservaConflictException cuando hay solapamiento de horario en la mesa")
    void crear_SolapamientoHorario_LanzaReservaConflictException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mesaRepository.findById(2L)).thenReturn(Optional.of(mesa));
        when(reservaRepository.existsOverlappingReserva(2L, fechaHora, null)).thenReturn(true);

        assertThrows(ReservaConflictException.class, () -> reservaService.crear(requestDTO));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("buscarPorId retorna ReservaResponseDTO cuando la reserva existe")
    void buscarPorId_Exitoso() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(responseDTO);

        ReservaResponseDTO result = reservaService.buscarPorId(10L);

        assertNotNull(result);
        assertEquals(10L, result.getIdReserva());
        assertEquals("Carlos Mendoza", result.getNombreCliente());
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
    @DisplayName("obtenerReservas sin filtros retorna todas las reservas")
    void obtenerReservas_SinFiltros_RetornaTodas() {
        List<Reserva> lista = List.of(reserva);
        List<ReservaResponseDTO> dtoList = List.of(responseDTO);

        when(reservaRepository.findAll()).thenReturn(lista);
        when(reservaMapper.toResponseDTOList(lista)).thenReturn(dtoList);

        List<ReservaResponseDTO> result = reservaService.obtenerReservas(null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservaRepository).findAll();
    }

    @Test
    @DisplayName("obtenerReservas con filtro por cliente retorna reservas de dicho cliente")
    void obtenerReservas_FiltroCliente_RetornaFiltradas() {
        List<Reserva> lista = List.of(reserva);
        List<ReservaResponseDTO> dtoList = List.of(responseDTO);

        when(reservaRepository.findByCliente(1L)).thenReturn(lista);
        when(reservaMapper.toResponseDTOList(lista)).thenReturn(dtoList);

        List<ReservaResponseDTO> result = reservaService.obtenerReservas(1L, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservaRepository).findByCliente(1L);
    }

    @Test
    @DisplayName("obtenerReservas con filtro por mesa retorna reservas asociadas a la mesa")
    void obtenerReservas_FiltroMesa_RetornaFiltradas() {
        List<Reserva> lista = List.of(reserva);
        List<ReservaResponseDTO> dtoList = List.of(responseDTO);

        when(reservaRepository.findByMesa(2L)).thenReturn(lista);
        when(reservaMapper.toResponseDTOList(lista)).thenReturn(dtoList);

        List<ReservaResponseDTO> result = reservaService.obtenerReservas(null, 2L, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservaRepository).findByMesa(2L);
    }

    @Test
    @DisplayName("obtenerReservas con filtro por estado retorna reservas con dicho estado")
    void obtenerReservas_FiltroEstado_RetornaFiltradas() {
        List<Reserva> lista = List.of(reserva);
        List<ReservaResponseDTO> dtoList = List.of(responseDTO);

        when(reservaRepository.findByEstado("PENDIENTE")).thenReturn(lista);
        when(reservaMapper.toResponseDTOList(lista)).thenReturn(dtoList);

        List<ReservaResponseDTO> result = reservaService.obtenerReservas(null, null, "PENDIENTE");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservaRepository).findByEstado("PENDIENTE");
    }

    @Test
    @DisplayName("actualizar modifica una reserva exitosamente cuando los datos son coherentes")
    void actualizar_Exitoso() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mesaRepository.findById(2L)).thenReturn(Optional.of(mesa));
        when(reservaRepository.existsOverlappingReserva(2L, fechaHora, 10L)).thenReturn(false);
        when(reservaRepository.update(reserva)).thenReturn(reserva);
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(responseDTO);

        ReservaResponseDTO result = reservaService.actualizar(10L, requestDTO);

        assertNotNull(result);
        verify(reservaRepository).update(reserva);
    }

    @Test
    @DisplayName("actualizar lanza ReservaNotFoundException cuando la reserva a modificar no existe")
    void actualizar_NoExiste_LanzaReservaNotFoundException() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservaNotFoundException.class, () -> reservaService.actualizar(99L, requestDTO));
        verify(reservaRepository, never()).update(any(Reserva.class));
    }

    @Test
    @DisplayName("actualizar lanza ClienteNotFoundException cuando el cliente no existe")
    void actualizar_ClienteNoExiste_LanzaClienteNotFoundException() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> reservaService.actualizar(10L, requestDTO));
        verify(reservaRepository, never()).update(any(Reserva.class));
    }

    @Test
    @DisplayName("actualizar lanza MesaNotFoundException cuando la mesa no existe")
    void actualizar_MesaNoExiste_LanzaMesaNotFoundException() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mesaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(MesaNotFoundException.class, () -> reservaService.actualizar(10L, requestDTO));
        verify(reservaRepository, never()).update(any(Reserva.class));
    }

    @Test
    @DisplayName("actualizar lanza ReservaConflictException cuando ocurre solapamiento de horario")
    void actualizar_SolapamientoHorario_LanzaReservaConflictException() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mesaRepository.findById(2L)).thenReturn(Optional.of(mesa));
        when(reservaRepository.existsOverlappingReserva(2L, fechaHora, 10L)).thenReturn(true);

        assertThrows(ReservaConflictException.class, () -> reservaService.actualizar(10L, requestDTO));
        verify(reservaRepository, never()).update(any(Reserva.class));
    }

    @Test
    @DisplayName("cambiarEstado actualiza el estado de la reserva exitosamente")
    void cambiarEstado_Exitoso() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.update(reserva)).thenReturn(reserva);
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(responseDTO);

        ReservaResponseDTO result = reservaService.cambiarEstado(10L, "CONFIRMADA");

        assertNotNull(result);
        assertEquals("CONFIRMADA", reserva.getEstado());
        verify(reservaRepository).update(reserva);
    }

    @Test
    @DisplayName("cambiarEstado lanza IllegalArgumentException cuando el estado es inválido")
    void cambiarEstado_EstadoInvalido_LanzaIllegalArgumentException() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));

        assertThrows(IllegalArgumentException.class, () -> reservaService.cambiarEstado(10L, "ESTADO_INEXISTENTE"));
        verify(reservaRepository, never()).update(any(Reserva.class));
    }

    @Test
    @DisplayName("cambiarEstado a CANCELADA actualiza el estado y envía correo de liberación de cupo")
    void cambiarEstado_Cancelada_EnviaCorreo() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.update(reserva)).thenReturn(reserva);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(responseDTO);

        ReservaResponseDTO result = reservaService.cambiarEstado(10L, "CANCELADA");

        assertNotNull(result);
        assertEquals("CANCELADA", reserva.getEstado());
        verify(reservaRepository).update(reserva);
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
    @DisplayName("eliminar borra la reserva cuando existe y envía notificación de cancelación")
    void eliminar_Exitoso() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        reservaService.eliminar(10L);

        verify(reservaRepository).deleteById(10L);
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
    @DisplayName("eliminar lanza ReservaNotFoundException cuando la reserva no existe")
    void eliminar_NoExiste_LanzaReservaNotFoundException() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservaNotFoundException.class, () -> reservaService.eliminar(99L));
        verify(reservaRepository, never()).deleteById(99L);
    }

    @Test
    @DisplayName("consultarMesasDisponibles retorna las mesas libres para el horario y capacidad requeridos")
    void consultarMesasDisponibles_Exitoso() {
        MesaResponseDTO mesaDTO = MesaResponseDTO.builder()
                .idMesa(2L)
                .numero(5)
                .capacidad(4)
                .estado("DISPONIBLE")
                .build();

        when(reservaRepository.findMesasDisponibles(fechaHora, 4)).thenReturn(List.of(mesa));
        when(mesaMapper.toResponseDTOList(List.of(mesa))).thenReturn(List.of(mesaDTO));

        List<MesaResponseDTO> result = reservaService.consultarMesasDisponibles(fechaHora, 4);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.getFirst().getNumero());
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
