package org.sabana.proyectofinal_bd1_back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sabana.proyectofinal_bd1_back.model.dto.CategoriaMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.CategoriaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.CategoriaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Categoria;
import org.sabana.proyectofinal_bd1_back.repository.CategoriaRepository;
import org.sabana.proyectofinal_bd1_back.util.exceptions.CategoriaAlreadyExistsException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.CategoriaNotFoundException;

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
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private CategoriaMapper categoriaMapper;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private Categoria categoria;
    private CategoriaRequestDTO requestDTO;
    private CategoriaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder()
                .idCategoria(1L)
                .nombre("Bebidas Calientes")
                .descripcion("Cafés espresso y lattes")
                .activo(true)
                .build();

        requestDTO = CategoriaRequestDTO.builder()
                .nombre("Bebidas Calientes")
                .descripcion("Cafés espresso y lattes")
                .activo(true)
                .build();

        responseDTO = CategoriaResponseDTO.builder()
                .idCategoria(1L)
                .nombre("Bebidas Calientes")
                .descripcion("Cafés espresso y lattes")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("buscarPorId retorna CategoriaResponseDTO cuando la categoría existe")
    void buscarPorId_Exitoso() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(responseDTO);

        CategoriaResponseDTO result = categoriaService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdCategoria());
        assertEquals("Bebidas Calientes", result.getNombre());
        verify(categoriaRepository).findById(1L);
    }

    @Test
    @DisplayName("buscarPorId lanza CategoriaNotFoundException cuando la categoría no existe")
    void buscarPorId_NoExiste_LanzaCategoriaNotFoundException() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoriaNotFoundException.class, () -> categoriaService.buscarPorId(99L));
        verify(categoriaRepository).findById(99L);
    }

    @Test
    @DisplayName("obtenerCategorias con soloActivas null retorna todas las categorías")
    void obtenerCategorias_SinFiltro_RetornaTodas() {
        List<Categoria> categorias = List.of(categoria);
        List<CategoriaResponseDTO> responseList = List.of(responseDTO);

        when(categoriaRepository.findAll()).thenReturn(categorias);
        when(categoriaMapper.toResponseDTOList(categorias)).thenReturn(responseList);

        List<CategoriaResponseDTO> result = categoriaService.obtenerCategorias(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(categoriaRepository).findAll();
    }

    @Test
    @DisplayName("obtenerCategorias con soloActivas false retorna todas las categorías")
    void obtenerCategorias_SoloActivasFalse_RetornaTodas() {
        List<Categoria> categorias = List.of(categoria);
        List<CategoriaResponseDTO> responseList = List.of(responseDTO);

        when(categoriaRepository.findAll()).thenReturn(categorias);
        when(categoriaMapper.toResponseDTOList(categorias)).thenReturn(responseList);

        List<CategoriaResponseDTO> result = categoriaService.obtenerCategorias(false);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(categoriaRepository).findAll();
    }

    @Test
    @DisplayName("obtenerCategorias con soloActivas true retorna solo las categorías activas")
    void obtenerCategorias_SoloActivasTrue_RetornaSoloActivas() {
        List<Categoria> categorias = List.of(categoria);
        List<CategoriaResponseDTO> responseList = List.of(responseDTO);

        when(categoriaRepository.findAllActivas()).thenReturn(categorias);
        when(categoriaMapper.toResponseDTOList(categorias)).thenReturn(responseList);

        List<CategoriaResponseDTO> result = categoriaService.obtenerCategorias(true);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(categoriaRepository).findAllActivas();
    }

    @Test
    @DisplayName("crear registra una categoría exitosamente cuando el nombre no está duplicado")
    void crear_Exitoso() {
        when(categoriaRepository.existsByNombre("Bebidas Calientes")).thenReturn(false);
        when(categoriaMapper.toEntity(requestDTO)).thenReturn(categoria);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(responseDTO);

        CategoriaResponseDTO result = categoriaService.crear(requestDTO);

        assertNotNull(result);
        assertEquals("Bebidas Calientes", result.getNombre());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    @DisplayName("crear lanza CategoriaAlreadyExistsException cuando el nombre ya está registrado")
    void crear_NombreDuplicado_LanzaCategoriaAlreadyExistsException() {
        when(categoriaRepository.existsByNombre("Bebidas Calientes")).thenReturn(true);

        assertThrows(CategoriaAlreadyExistsException.class, () -> categoriaService.crear(requestDTO));
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("actualizar modifica una categoría exitosamente cuando el nombre no está duplicado")
    void actualizar_Exitoso() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreAndNotId("Bebidas Calientes", 1L)).thenReturn(false);
        when(categoriaRepository.update(categoria)).thenReturn(categoria);
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(responseDTO);

        CategoriaResponseDTO result = categoriaService.actualizar(1L, requestDTO);

        assertNotNull(result);
        verify(categoriaRepository).update(categoria);
    }

    @Test
    @DisplayName("actualizar lanza CategoriaNotFoundException cuando la categoría a actualizar no existe")
    void actualizar_NoExiste_LanzaCategoriaNotFoundException() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoriaNotFoundException.class, () -> categoriaService.actualizar(99L, requestDTO));
        verify(categoriaRepository, never()).update(any(Categoria.class));
    }

    @Test
    @DisplayName("actualizar lanza CategoriaAlreadyExistsException si otra categoría ya tiene ese nombre")
    void actualizar_NombreDuplicado_LanzaCategoriaAlreadyExistsException() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreAndNotId("Bebidas Calientes", 1L)).thenReturn(true);

        assertThrows(CategoriaAlreadyExistsException.class, () -> categoriaService.actualizar(1L, requestDTO));
        verify(categoriaRepository, never()).update(any(Categoria.class));
    }

    @Test
    @DisplayName("eliminar borra la categoría cuando existe")
    void eliminar_Exitoso() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        categoriaService.eliminar(1L);

        verify(categoriaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar lanza CategoriaNotFoundException cuando la categoría no existe")
    void eliminar_NoExiste_LanzaCategoriaNotFoundException() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoriaNotFoundException.class, () -> categoriaService.eliminar(99L));
        verify(categoriaRepository, never()).deleteById(99L);
    }

}
