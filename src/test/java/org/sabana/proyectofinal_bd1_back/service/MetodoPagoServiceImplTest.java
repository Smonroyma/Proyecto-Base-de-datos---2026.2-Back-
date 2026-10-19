package org.sabana.proyectofinal_bd1_back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.MetodoPago;
import org.sabana.proyectofinal_bd1_back.repository.MetodoPagoRepository;
import org.sabana.proyectofinal_bd1_back.util.exceptions.DuplicateResourceException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.MetodoPagoNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetodoPagoServiceImplTest {

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private MetodoPagoMapper metodoPagoMapper;

    @InjectMocks
    private MetodoPagoServiceImpl metodoPagoService;

    private MetodoPago metodoPago;
    private MetodoPagoRequestDTO requestDTO;
    private MetodoPagoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        metodoPago = MetodoPago.builder()
                .tipo("EFECTIVO")
                .nombre("Efectivo")
                .descripcion("Pago en moneda local en caja")
                .activo(true)
                .build();

        requestDTO = MetodoPagoRequestDTO.builder()
                .tipo("  efectivo  ")
                .nombre("  Efectivo  ")
                .descripcion("  Pago en moneda local en caja  ")
                .activo(true)
                .build();

        responseDTO = MetodoPagoResponseDTO.builder()
                .tipo("EFECTIVO")
                .nombre("Efectivo")
                .descripcion("Pago en moneda local en caja")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("buscarPorTipo retorna MetodoPagoResponseDTO cuando el tipo existe")
    void buscarPorTipo_Exitoso() {
        when(metodoPagoRepository.findByTipo("EFECTIVO")).thenReturn(Optional.of(metodoPago));
        when(metodoPagoMapper.toResponseDTO(metodoPago)).thenReturn(responseDTO);

        MetodoPagoResponseDTO result = metodoPagoService.buscarPorTipo("EFECTIVO");

        assertNotNull(result);
        assertEquals("EFECTIVO", result.getTipo());
        verify(metodoPagoRepository).findByTipo("EFECTIVO");
    }

    @Test
    @DisplayName("buscarPorTipo lanza MetodoPagoNotFoundException cuando el tipo no existe")
    void buscarPorTipo_NoExiste_LanzaExcepcion() {
        when(metodoPagoRepository.findByTipo("BITCOIN")).thenReturn(Optional.empty());

        assertThrows(MetodoPagoNotFoundException.class, () -> metodoPagoService.buscarPorTipo("BITCOIN"));
    }

    @Test
    @DisplayName("obtenerMetodosPago sin filtro retorna todos los métodos de pago")
    void obtenerMetodosPago_SinFiltro_RetornaTodos() {
        List<MetodoPago> lista = List.of(metodoPago);
        when(metodoPagoRepository.findAll()).thenReturn(lista);
        when(metodoPagoMapper.toResponseDTOList(lista)).thenReturn(List.of(responseDTO));

        List<MetodoPagoResponseDTO> result = metodoPagoService.obtenerMetodosPago(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(metodoPagoRepository).findAll();
        verify(metodoPagoRepository, never()).findAllActivos();
    }

    @Test
    @DisplayName("obtenerMetodosPago con soloActivos true retorna únicamente los métodos activos")
    void obtenerMetodosPago_SoloActivosTrue_RetornaActivos() {
        List<MetodoPago> lista = List.of(metodoPago);
        when(metodoPagoRepository.findAllActivos()).thenReturn(lista);
        when(metodoPagoMapper.toResponseDTOList(lista)).thenReturn(List.of(responseDTO));

        List<MetodoPagoResponseDTO> result = metodoPagoService.obtenerMetodosPago(true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.getFirst().getActivo());
        verify(metodoPagoRepository).findAllActivos();
        verify(metodoPagoRepository, never()).findAll();
    }

    @Test
    @DisplayName("crear registra un nuevo método de pago normalizando el tipo a mayúsculas")
    void crear_Exitoso() {
        when(metodoPagoRepository.existsByTipo("EFECTIVO")).thenReturn(false);
        when(metodoPagoMapper.toEntity(requestDTO)).thenReturn(metodoPago);
        when(metodoPagoRepository.save(any(MetodoPago.class))).thenReturn(metodoPago);
        when(metodoPagoMapper.toResponseDTO(metodoPago)).thenReturn(responseDTO);

        MetodoPagoResponseDTO result = metodoPagoService.crear(requestDTO);

        assertNotNull(result);
        assertEquals("EFECTIVO", result.getTipo());
        verify(metodoPagoRepository).existsByTipo("EFECTIVO");
        verify(metodoPagoRepository).save(metodoPago);
    }

    @Test
    @DisplayName("crear lanza DuplicateResourceException cuando el tipo ya está registrado")
    void crear_TipoDuplicado_LanzaDuplicateResourceException() {
        when(metodoPagoRepository.existsByTipo("EFECTIVO")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> metodoPagoService.crear(requestDTO));
        verify(metodoPagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar modifica exitosamente un método de pago existente")
    void actualizar_Exitoso() {
        when(metodoPagoRepository.findByTipo("EFECTIVO")).thenReturn(Optional.of(metodoPago));
        doNothing().when(metodoPagoMapper).updateEntityFromDTO(requestDTO, metodoPago);
        when(metodoPagoRepository.update(eq("EFECTIVO"), any(MetodoPago.class))).thenReturn(metodoPago);
        when(metodoPagoMapper.toResponseDTO(metodoPago)).thenReturn(responseDTO);

        MetodoPagoResponseDTO result = metodoPagoService.actualizar("EFECTIVO", requestDTO);

        assertNotNull(result);
        assertEquals("EFECTIVO", result.getTipo());
        verify(metodoPagoRepository).update(eq("EFECTIVO"), any(MetodoPago.class));
    }

    @Test
    @DisplayName("eliminar borra el método de pago cuando el tipo existe")
    void eliminar_Exitoso() {
        when(metodoPagoRepository.findByTipo("EFECTIVO")).thenReturn(Optional.of(metodoPago));
        when(metodoPagoRepository.deleteByTipo("EFECTIVO")).thenReturn(true);

        metodoPagoService.eliminar("EFECTIVO");

        verify(metodoPagoRepository).deleteByTipo("EFECTIVO");
    }

    @Test
    @DisplayName("eliminar lanza MetodoPagoNotFoundException cuando el tipo no existe")
    void eliminar_NoExiste_LanzaMetodoPagoNotFoundException() {
        when(metodoPagoRepository.findByTipo("INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(MetodoPagoNotFoundException.class, () -> metodoPagoService.eliminar("INEXISTENTE"));
        verify(metodoPagoRepository, never()).deleteByTipo(any());
    }

}
