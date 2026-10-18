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
                .idMetodoPago(1L)
                .codigo("EFECTIVO")
                .nombre("Efectivo")
                .descripcion("Pago en moneda local en caja")
                .activo(true)
                .build();

        requestDTO = MetodoPagoRequestDTO.builder()
                .codigo("  efectivo  ")
                .nombre("  Efectivo  ")
                .descripcion("  Pago en moneda local en caja  ")
                .activo(true)
                .build();

        responseDTO = MetodoPagoResponseDTO.builder()
                .idMetodoPago(1L)
                .codigo("EFECTIVO")
                .nombre("Efectivo")
                .descripcion("Pago en moneda local en caja")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("buscarPorId retorna MetodoPagoResponseDTO cuando el método de pago existe")
    void buscarPorId_Exitoso() {
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        when(metodoPagoMapper.toResponseDTO(metodoPago)).thenReturn(responseDTO);

        MetodoPagoResponseDTO result = metodoPagoService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdMetodoPago());
        assertEquals("EFECTIVO", result.getCodigo());
        verify(metodoPagoRepository).findById(1L);
    }

    @Test
    @DisplayName("buscarPorId lanza MetodoPagoNotFoundException cuando el ID no existe")
    void buscarPorId_NoExiste_LanzaExcepcion() {
        when(metodoPagoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MetodoPagoNotFoundException.class, () -> metodoPagoService.buscarPorId(99L));
        verify(metodoPagoRepository).findById(99L);
    }

    @Test
    @DisplayName("buscarPorCodigo retorna MetodoPagoResponseDTO cuando el código existe")
    void buscarPorCodigo_Exitoso() {
        when(metodoPagoRepository.findByCodigo("EFECTIVO")).thenReturn(Optional.of(metodoPago));
        when(metodoPagoMapper.toResponseDTO(metodoPago)).thenReturn(responseDTO);

        MetodoPagoResponseDTO result = metodoPagoService.buscarPorCodigo("EFECTIVO");

        assertNotNull(result);
        assertEquals("EFECTIVO", result.getCodigo());
        verify(metodoPagoRepository).findByCodigo("EFECTIVO");
    }

    @Test
    @DisplayName("buscarPorCodigo lanza MetodoPagoNotFoundException cuando el código no existe")
    void buscarPorCodigo_NoExiste_LanzaExcepcion() {
        when(metodoPagoRepository.findByCodigo("BITCOIN")).thenReturn(Optional.empty());

        assertThrows(MetodoPagoNotFoundException.class, () -> metodoPagoService.buscarPorCodigo("BITCOIN"));
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
    @DisplayName("obtenerMetodosPago con soloActivos false retorna todos los métodos de pago")
    void obtenerMetodosPago_SoloActivosFalse_RetornaTodos() {
        List<MetodoPago> lista = List.of(metodoPago);
        when(metodoPagoRepository.findAll()).thenReturn(lista);
        when(metodoPagoMapper.toResponseDTOList(lista)).thenReturn(List.of(responseDTO));

        List<MetodoPagoResponseDTO> result = metodoPagoService.obtenerMetodosPago(false);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(metodoPagoRepository).findAll();
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
        assertTrue(result.get(0).getActivo());
        verify(metodoPagoRepository).findAllActivos();
        verify(metodoPagoRepository, never()).findAll();
    }

    @Test
    @DisplayName("crear registra un nuevo método de pago normalizando el código a mayúsculas")
    void crear_Exitoso() {
        when(metodoPagoRepository.existsByCodigo("EFECTIVO")).thenReturn(false);
        when(metodoPagoMapper.toEntity(requestDTO)).thenReturn(metodoPago);
        when(metodoPagoRepository.save(any(MetodoPago.class))).thenReturn(metodoPago);
        when(metodoPagoMapper.toResponseDTO(metodoPago)).thenReturn(responseDTO);

        MetodoPagoResponseDTO result = metodoPagoService.crear(requestDTO);

        assertNotNull(result);
        assertEquals("EFECTIVO", result.getCodigo());
        assertEquals("Efectivo", result.getNombre());
        verify(metodoPagoRepository).existsByCodigo("EFECTIVO");
        verify(metodoPagoRepository).save(metodoPago);
    }

    @Test
    @DisplayName("crear lanza DuplicateResourceException cuando el código ya está registrado")
    void crear_CodigoDuplicado_LanzaDuplicateResourceException() {
        when(metodoPagoRepository.existsByCodigo("EFECTIVO")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> metodoPagoService.crear(requestDTO));
        verify(metodoPagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar modifica exitosamente un método de pago existente")
    void actualizar_Exitoso() {
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        when(metodoPagoRepository.existsByCodigoAndNotId("EFECTIVO", 1L)).thenReturn(false);
        doNothing().when(metodoPagoMapper).updateEntityFromDTO(requestDTO, metodoPago);
        when(metodoPagoRepository.update(metodoPago)).thenReturn(metodoPago);
        when(metodoPagoMapper.toResponseDTO(metodoPago)).thenReturn(responseDTO);

        MetodoPagoResponseDTO result = metodoPagoService.actualizar(1L, requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getIdMetodoPago());
        assertEquals("EFECTIVO", result.getCodigo());
        verify(metodoPagoRepository).update(metodoPago);
    }

    @Test
    @DisplayName("actualizar lanza MetodoPagoNotFoundException cuando el ID no existe")
    void actualizar_NoExiste_LanzaMetodoPagoNotFoundException() {
        when(metodoPagoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MetodoPagoNotFoundException.class, () -> metodoPagoService.actualizar(99L, requestDTO));
        verify(metodoPagoRepository, never()).update(any());
    }

    @Test
    @DisplayName("actualizar lanza DuplicateResourceException cuando el código pertenece a otro método de pago")
    void actualizar_CodigoDuplicadoEnOtroRegistro_LanzaDuplicateResourceException() {
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        when(metodoPagoRepository.existsByCodigoAndNotId("EFECTIVO", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> metodoPagoService.actualizar(1L, requestDTO));
        verify(metodoPagoRepository, never()).update(any());
    }

    @Test
    @DisplayName("eliminar borra el método de pago cuando el ID existe")
    void eliminar_Exitoso() {
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        when(metodoPagoRepository.deleteById(1L)).thenReturn(true);

        metodoPagoService.eliminar(1L);

        verify(metodoPagoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar lanza MetodoPagoNotFoundException cuando el ID no existe")
    void eliminar_NoExiste_LanzaMetodoPagoNotFoundException() {
        when(metodoPagoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MetodoPagoNotFoundException.class, () -> metodoPagoService.eliminar(99L));
        verify(metodoPagoRepository, never()).deleteById(any());
    }

}
