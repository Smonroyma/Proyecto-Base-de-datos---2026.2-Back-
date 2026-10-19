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
import static org.mockito.ArgumentMatchers.eq;
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
                .nombreCategoria("Bebidas Calientes")
                .descripcion("Cafés espresso y lattes")
                .estado("ACTIVA")
                .build();

        requestDTO = CategoriaRequestDTO.builder()
                .nombreCategoria("Bebidas Calientes")
                .descripcion("Cafés espresso y lattes")
                .estado("ACTIVA")
                .build();

        responseDTO = CategoriaResponseDTO.builder()
                .nombreCategoria("Bebidas Calientes")
                .descripcion("Cafés espresso y lattes")
                .estado("ACTIVA")
                .build();
    }

    @Test
    @DisplayName("buscarPorNombre retorna CategoriaResponseDTO cuando la categoría existe")
    void buscarPorNombre_Exitoso() {
        when(categoriaRepository.findByNombreCategoria("Bebidas Calientes")).thenReturn(Optional.of(categoria));
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(responseDTO);

        CategoriaResponseDTO result = categoriaService.buscarPorNombre("Bebidas Calientes");

        assertNotNull(result);
        assertEquals("Bebidas Calientes", result.getNombreCategoria());
        verify(categoriaRepository).findByNombreCategoria("Bebidas Calientes");
    }

    @Test
    @DisplayName("buscarPorNombre lanza CategoriaNotFoundException cuando la categoría no existe")
    void buscarPorNombre_NoExiste_LanzaCategoriaNotFoundException() {
        when(categoriaRepository.findByNombreCategoria("Inexistente")).thenReturn(Optional.empty());

        assertThrows(CategoriaNotFoundException.class, () -> categoriaService.buscarPorNombre("Inexistente"));
        verify(categoriaRepository).findByNombreCategoria("Inexistente");
    }

    @Test
    @DisplayName("obtenerCategorias con estado null retorna todas las categorías")
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
    @DisplayName("obtenerCategorias con estado ACTIVA retorna solo las categorías activas")
    void obtenerCategorias_EstadoActiva_RetornaFiltradas() {
        List<Categoria> categorias = List.of(categoria);
        List<CategoriaResponseDTO> responseList = List.of(responseDTO);

        when(categoriaRepository.findByEstado("ACTIVA")).thenReturn(categorias);
        when(categoriaMapper.toResponseDTOList(categorias)).thenReturn(responseList);

        List<CategoriaResponseDTO> result = categoriaService.obtenerCategorias("ACTIVA");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(categoriaRepository).findByEstado("ACTIVA");
    }

    @Test
    @DisplayName("crear registra una categoría exitosamente cuando el nombre no está duplicado")
    void crear_Exitoso() {
        when(categoriaRepository.existsByNombreCategoria("Bebidas Calientes")).thenReturn(false);
        when(categoriaMapper.toEntity(requestDTO)).thenReturn(categoria);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(responseDTO);

        CategoriaResponseDTO result = categoriaService.crear(requestDTO);

        assertNotNull(result);
        assertEquals("Bebidas Calientes", result.getNombreCategoria());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    @DisplayName("crear lanza CategoriaAlreadyExistsException cuando el nombre ya está registrado")
    void crear_NombreDuplicado_LanzaCategoriaAlreadyExistsException() {
        when(categoriaRepository.existsByNombreCategoria("Bebidas Calientes")).thenReturn(true);

        assertThrows(CategoriaAlreadyExistsException.class, () -> categoriaService.crear(requestDTO));
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("actualizar modifica una categoría exitosamente")
    void actualizar_Exitoso() {
        when(categoriaRepository.findByNombreCategoria("Bebidas Calientes")).thenReturn(Optional.of(categoria));
        when(categoriaRepository.update(eq("Bebidas Calientes"), any(Categoria.class))).thenReturn(categoria);
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(responseDTO);

        CategoriaResponseDTO result = categoriaService.actualizar("Bebidas Calientes", requestDTO);

        assertNotNull(result);
        verify(categoriaRepository).update(eq("Bebidas Calientes"), any(Categoria.class));
    }

    @Test
    @DisplayName("actualizar lanza CategoriaNotFoundException cuando la categoría a actualizar no existe")
    void actualizar_NoExiste_LanzaCategoriaNotFoundException() {
        when(categoriaRepository.findByNombreCategoria("Inexistente")).thenReturn(Optional.empty());

        assertThrows(CategoriaNotFoundException.class, () -> categoriaService.actualizar("Inexistente", requestDTO));
        verify(categoriaRepository, never()).update(any(), any(Categoria.class));
    }

    @Test
    @DisplayName("actualizar lanza CategoriaAlreadyExistsException si se renombra a otra categoría existente")
    void actualizar_NombreDuplicado_LanzaCategoriaAlreadyExistsException() {
        requestDTO.setNombreCategoria("Postres");
        when(categoriaRepository.findByNombreCategoria("Bebidas Calientes")).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreCategoria("Postres")).thenReturn(true);

        assertThrows(CategoriaAlreadyExistsException.class, () -> categoriaService.actualizar("Bebidas Calientes", requestDTO));
        verify(categoriaRepository, never()).update(any(), any(Categoria.class));
    }

    @Test
    @DisplayName("eliminar borra la categoría cuando existe")
    void eliminar_Exitoso() {
        when(categoriaRepository.findByNombreCategoria("Bebidas Calientes")).thenReturn(Optional.of(categoria));

        categoriaService.eliminar("Bebidas Calientes");

        verify(categoriaRepository).deleteByNombreCategoria("Bebidas Calientes");
    }

    @Test
    @DisplayName("eliminar lanza CategoriaNotFoundException cuando la categoría no existe")
    void eliminar_NoExiste_LanzaCategoriaNotFoundException() {
        when(categoriaRepository.findByNombreCategoria("Inexistente")).thenReturn(Optional.empty());

        assertThrows(CategoriaNotFoundException.class, () -> categoriaService.eliminar("Inexistente"));
        verify(categoriaRepository, never()).deleteByNombreCategoria("Inexistente");
    }

}
