package org.sabana.proyectofinal_bd1_back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sabana.proyectofinal_bd1_back.model.dto.MesaMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.MesaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.MesaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;
import org.sabana.proyectofinal_bd1_back.repository.MesaRepository;
import org.sabana.proyectofinal_bd1_back.util.exceptions.MesaAlreadyExistsException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.MesaNotFoundException;

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
class MesaServiceImplTest {

    @Mock
    private MesaRepository mesaRepository;

    @Mock
    private MesaMapper mesaMapper;

    @InjectMocks
    private MesaServiceImpl mesaService;

    private Mesa mesa;
    private MesaRequestDTO requestDTO;
    private MesaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        mesa = Mesa.builder()
                .numeroMesa(5)
                .capacidad(4)
                .estado("DISPONIBLE")
                .build();

        requestDTO = MesaRequestDTO.builder()
                .numeroMesa(5)
                .capacidad(4)
                .estado("DISPONIBLE")
                .build();

        responseDTO = MesaResponseDTO.builder()
                .numeroMesa(5)
                .capacidad(4)
                .estado("DISPONIBLE")
                .build();
    }

    @Test
    @DisplayName("buscarPorNumero retorna MesaResponseDTO cuando la mesa existe")
    void buscarPorNumero_Exitoso() {
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));
        when(mesaMapper.toResponseDTO(mesa)).thenReturn(responseDTO);

        MesaResponseDTO result = mesaService.buscarPorNumero(5);

        assertNotNull(result);
        assertEquals(5, result.getNumeroMesa());
        verify(mesaRepository).findByNumeroMesa(5);
    }

    @Test
    @DisplayName("buscarPorNumero lanza MesaNotFoundException cuando el número no existe")
    void buscarPorNumero_NoExiste_LanzaMesaNotFoundException() {
        when(mesaRepository.findByNumeroMesa(999)).thenReturn(Optional.empty());

        assertThrows(MesaNotFoundException.class, () -> mesaService.buscarPorNumero(999));
        verify(mesaRepository).findByNumeroMesa(999);
    }

    @Test
    @DisplayName("obtenerMesas sin filtro retorna todas las mesas")
    void obtenerMesas_SinFiltro_RetornaTodas() {
        List<Mesa> mesas = List.of(mesa);
        List<MesaResponseDTO> responseList = List.of(responseDTO);

        when(mesaRepository.findAll()).thenReturn(mesas);
        when(mesaMapper.toResponseDTOList(mesas)).thenReturn(responseList);

        List<MesaResponseDTO> result = mesaService.obtenerMesas(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mesaRepository).findAll();
    }

    @Test
    @DisplayName("obtenerMesas con filtro de estado retorna mesas filtradas")
    void obtenerMesas_ConFiltroEstado_RetornaFiltradas() {
        List<Mesa> mesas = List.of(mesa);
        List<MesaResponseDTO> responseList = List.of(responseDTO);

        when(mesaRepository.findByEstado("DISPONIBLE")).thenReturn(mesas);
        when(mesaMapper.toResponseDTOList(mesas)).thenReturn(responseList);

        List<MesaResponseDTO> result = mesaService.obtenerMesas("DISPONIBLE");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mesaRepository).findByEstado("DISPONIBLE");
    }

    @Test
    @DisplayName("obtenerMesas con estado no permitido lanza IllegalArgumentException")
    void obtenerMesas_EstadoInvalido_LanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> mesaService.obtenerMesas("ESTADO_INEXISTENTE"));
        verify(mesaRepository, never()).findByEstado(any());
    }

    @Test
    @DisplayName("crear registra una mesa exitosamente")
    void crear_Exitoso() {
        when(mesaRepository.existsByNumeroMesa(requestDTO.getNumeroMesa())).thenReturn(false);
        when(mesaMapper.toEntity(requestDTO)).thenReturn(mesa);
        when(mesaRepository.save(any(Mesa.class))).thenReturn(mesa);
        when(mesaMapper.toResponseDTO(mesa)).thenReturn(responseDTO);

        MesaResponseDTO result = mesaService.crear(requestDTO);

        assertNotNull(result);
        assertEquals(5, result.getNumeroMesa());
        verify(mesaRepository).save(any(Mesa.class));
    }

    @Test
    @DisplayName("crear lanza MesaAlreadyExistsException cuando el número de mesa ya está registrado")
    void crear_NumeroDuplicado_LanzaMesaAlreadyExistsException() {
        when(mesaRepository.existsByNumeroMesa(requestDTO.getNumeroMesa())).thenReturn(true);

        assertThrows(MesaAlreadyExistsException.class, () -> mesaService.crear(requestDTO));
        verify(mesaRepository, never()).save(any(Mesa.class));
    }

    @Test
    @DisplayName("crear lanza IllegalArgumentException cuando el estado no es válido")
    void crear_EstadoInvalido_LanzaIllegalArgumentException() {
        requestDTO.setEstado("INCORRECTO");

        assertThrows(IllegalArgumentException.class, () -> mesaService.crear(requestDTO));
        verify(mesaRepository, never()).save(any(Mesa.class));
    }

    @Test
    @DisplayName("actualizar modifica una mesa exitosamente")
    void actualizar_Exitoso() {
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));
        when(mesaRepository.update(mesa)).thenReturn(mesa);
        when(mesaMapper.toResponseDTO(mesa)).thenReturn(responseDTO);

        MesaResponseDTO result = mesaService.actualizar(5, requestDTO);

        assertNotNull(result);
        verify(mesaRepository).update(mesa);
    }

    @Test
    @DisplayName("actualizar lanza MesaNotFoundException si la mesa no existe")
    void actualizar_NoExiste_LanzaMesaNotFoundException() {
        when(mesaRepository.findByNumeroMesa(99)).thenReturn(Optional.empty());

        assertThrows(MesaNotFoundException.class, () -> mesaService.actualizar(99, requestDTO));
        verify(mesaRepository, never()).update(any(Mesa.class));
    }

    @Test
    @DisplayName("actualizarEstado modifica el estado de una mesa exitosamente")
    void actualizarEstado_Exitoso() {
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));
        when(mesaMapper.toResponseDTO(mesa)).thenReturn(responseDTO);

        MesaResponseDTO result = mesaService.actualizarEstado(5, "OCUPADA");

        assertNotNull(result);
        verify(mesaRepository).updateEstado(5, "OCUPADA");
    }

    @Test
    @DisplayName("actualizar lanza IllegalArgumentException si el nuevo estado es inválido")
    void actualizar_EstadoInvalido_LanzaIllegalArgumentException() {
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));
        requestDTO.setEstado("INVALIDO");

        assertThrows(IllegalArgumentException.class, () -> mesaService.actualizar(5, requestDTO));
        verify(mesaRepository, never()).update(any(Mesa.class));
    }

    @Test
    @DisplayName("eliminar borra la mesa cuando existe")
    void eliminar_Exitoso() {
        when(mesaRepository.findByNumeroMesa(5)).thenReturn(Optional.of(mesa));

        mesaService.eliminar(5);

        verify(mesaRepository).deleteByNumeroMesa(5);
    }

    @Test
    @DisplayName("eliminar lanza MesaNotFoundException cuando la mesa no existe")
    void eliminar_NoExiste_LanzaMesaNotFoundException() {
        when(mesaRepository.findByNumeroMesa(99)).thenReturn(Optional.empty());

        assertThrows(MesaNotFoundException.class, () -> mesaService.eliminar(99));
        verify(mesaRepository, never()).deleteByNumeroMesa(99);
    }

}
