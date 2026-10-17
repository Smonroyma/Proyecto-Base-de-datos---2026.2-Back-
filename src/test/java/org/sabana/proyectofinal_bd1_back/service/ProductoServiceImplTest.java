package org.sabana.proyectofinal_bd1_back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sabana.proyectofinal_bd1_back.model.dto.ProductoMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.ProductoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ProductoResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Categoria;
import org.sabana.proyectofinal_bd1_back.model.entity.Producto;
import org.sabana.proyectofinal_bd1_back.repository.CategoriaRepository;
import org.sabana.proyectofinal_bd1_back.repository.ProductoRepository;
import org.sabana.proyectofinal_bd1_back.util.exceptions.CategoriaNotFoundException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ProductoAlreadyExistsException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ProductoNotFoundException;

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
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto producto;
    private Categoria categoria;
    private ProductoRequestDTO requestDTO;
    private ProductoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder()
                .idCategoria(1L)
                .nombre("Bebidas Calientes")
                .descripcion("Cafés espresso y lattes")
                .activo(true)
                .build();

        producto = Producto.builder()
                .idProducto(1L)
                .nombre("Café Americano")
                .descripcion("Espresso doble diluido con agua caliente")
                .precio(6500.0)
                .idCategoria(1L)
                .nombreCategoria("Bebidas Calientes")
                .imagenUrl("https://imagenes.savanacoffee.com/americano.jpg")
                .disponible(true)
                .stock(50)
                .build();

        requestDTO = ProductoRequestDTO.builder()
                .nombre("Café Americano")
                .descripcion("Espresso doble diluido con agua caliente")
                .precio(6500.0)
                .idCategoria(1L)
                .imagenUrl("https://imagenes.savanacoffee.com/americano.jpg")
                .disponible(true)
                .stock(50)
                .build();

        responseDTO = ProductoResponseDTO.builder()
                .idProducto(1L)
                .nombre("Café Americano")
                .descripcion("Espresso doble diluido con agua caliente")
                .precio(6500.0)
                .idCategoria(1L)
                .nombreCategoria("Bebidas Calientes")
                .imagenUrl("https://imagenes.savanacoffee.com/americano.jpg")
                .disponible(true)
                .stock(50)
                .build();
    }

    @Test
    @DisplayName("buscarPorId retorna ProductoResponseDTO cuando el producto existe")
    void buscarPorId_Exitoso() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoMapper.toResponseDTO(producto)).thenReturn(responseDTO);

        ProductoResponseDTO result = productoService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdProducto());
        assertEquals("Café Americano", result.getNombre());
        assertEquals("Bebidas Calientes", result.getNombreCategoria());
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("buscarPorId lanza ProductoNotFoundException cuando el producto no existe")
    void buscarPorId_NoExiste_LanzaProductoNotFoundException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> productoService.buscarPorId(99L));
        verify(productoRepository).findById(99L);
    }

    @Test
    @DisplayName("obtenerProductos sin filtros retorna todos los productos")
    void obtenerProductos_SinFiltros_RetornaTodos() {
        List<Producto> productos = List.of(producto);
        List<ProductoResponseDTO> responseList = List.of(responseDTO);

        when(productoRepository.findAll()).thenReturn(productos);
        when(productoMapper.toResponseDTOList(productos)).thenReturn(responseList);

        List<ProductoResponseDTO> result = productoService.obtenerProductos(null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productoRepository).findAll();
    }

    @Test
    @DisplayName("obtenerProductos filtrando solo por categoría retorna los productos de dicha categoría")
    void obtenerProductos_SoloCategoria_RetornaFiltrados() {
        List<Producto> productos = List.of(producto);
        List<ProductoResponseDTO> responseList = List.of(responseDTO);

        when(productoRepository.findByCategoria(1L)).thenReturn(productos);
        when(productoMapper.toResponseDTOList(productos)).thenReturn(responseList);

        List<ProductoResponseDTO> result = productoService.obtenerProductos(1L, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productoRepository).findByCategoria(1L);
    }

    @Test
    @DisplayName("obtenerProductos filtrando solo por disponibilidad retorna los productos correspondientes")
    void obtenerProductos_SoloDisponible_RetornaFiltrados() {
        List<Producto> productos = List.of(producto);
        List<ProductoResponseDTO> responseList = List.of(responseDTO);

        when(productoRepository.findByDisponible(true)).thenReturn(productos);
        when(productoMapper.toResponseDTOList(productos)).thenReturn(responseList);

        List<ProductoResponseDTO> result = productoService.obtenerProductos(null, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productoRepository).findByDisponible(true);
    }

    @Test
    @DisplayName("obtenerProductos con ambos filtros retorna productos coincidentes con categoría y disponibilidad")
    void obtenerProductos_AmbosFiltros_RetornaFiltrados() {
        List<Producto> productos = List.of(producto);
        List<ProductoResponseDTO> responseList = List.of(responseDTO);

        when(productoRepository.findByCategoriaAndDisponible(1L, true)).thenReturn(productos);
        when(productoMapper.toResponseDTOList(productos)).thenReturn(responseList);

        List<ProductoResponseDTO> result = productoService.obtenerProductos(1L, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productoRepository).findByCategoriaAndDisponible(1L, true);
    }

    @Test
    @DisplayName("crear registra un producto exitosamente cuando la categoría existe y el nombre no está duplicado")
    void crear_Exitoso() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.existsByNombre("Café Americano")).thenReturn(false);
        when(productoMapper.toEntity(requestDTO)).thenReturn(producto);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(productoMapper.toResponseDTO(producto)).thenReturn(responseDTO);

        ProductoResponseDTO result = productoService.crear(requestDTO);

        assertNotNull(result);
        assertEquals("Café Americano", result.getNombre());
        verify(categoriaRepository).findById(1L);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("crear lanza CategoriaNotFoundException cuando la categoría especificada no existe")
    void crear_CategoriaNoExiste_LanzaCategoriaNotFoundException() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());
        requestDTO.setIdCategoria(99L);

        assertThrows(CategoriaNotFoundException.class, () -> productoService.crear(requestDTO));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("crear lanza ProductoAlreadyExistsException cuando el nombre del producto ya existe")
    void crear_NombreDuplicado_LanzaProductoAlreadyExistsException() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.existsByNombre("Café Americano")).thenReturn(true);

        assertThrows(ProductoAlreadyExistsException.class, () -> productoService.crear(requestDTO));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("actualizar modifica un producto exitosamente cuando producto y categoría existen y el nombre no colisiona")
    void actualizar_Exitoso() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.existsByNombreAndNotId("Café Americano", 1L)).thenReturn(false);
        when(productoRepository.update(producto)).thenReturn(producto);
        when(productoMapper.toResponseDTO(producto)).thenReturn(responseDTO);

        ProductoResponseDTO result = productoService.actualizar(1L, requestDTO);

        assertNotNull(result);
        verify(productoRepository).update(producto);
    }

    @Test
    @DisplayName("actualizar lanza ProductoNotFoundException cuando el producto a modificar no existe")
    void actualizar_ProductoNoExiste_LanzaProductoNotFoundException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> productoService.actualizar(99L, requestDTO));
        verify(categoriaRepository, never()).findById(any());
        verify(productoRepository, never()).update(any(Producto.class));
    }

    @Test
    @DisplayName("actualizar lanza CategoriaNotFoundException cuando la categoría indicada no existe")
    void actualizar_CategoriaNoExiste_LanzaCategoriaNotFoundException() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoriaNotFoundException.class, () -> productoService.actualizar(1L, requestDTO));
        verify(productoRepository, never()).update(any(Producto.class));
    }

    @Test
    @DisplayName("actualizar lanza ProductoAlreadyExistsException cuando otro producto ya posee el mismo nombre")
    void actualizar_NombreDuplicado_LanzaProductoAlreadyExistsException() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.existsByNombreAndNotId("Café Americano", 1L)).thenReturn(true);

        assertThrows(ProductoAlreadyExistsException.class, () -> productoService.actualizar(1L, requestDTO));
        verify(productoRepository, never()).update(any(Producto.class));
    }

    @Test
    @DisplayName("eliminar borra el producto cuando existe")
    void eliminar_Exitoso() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        productoService.eliminar(1L);

        verify(productoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar lanza ProductoNotFoundException cuando el producto no existe")
    void eliminar_NoExiste_LanzaProductoNotFoundException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> productoService.eliminar(99L));
        verify(productoRepository, never()).deleteById(99L);
    }

}
