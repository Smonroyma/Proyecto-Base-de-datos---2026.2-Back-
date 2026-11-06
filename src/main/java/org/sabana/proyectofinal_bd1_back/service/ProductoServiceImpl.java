package org.sabana.proyectofinal_bd1_back.service;

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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoMapper productoMapper;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               CategoriaRepository categoriaRepository,
                               ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoMapper = productoMapper;
    }

    @Override
    public List<ProductoResponseDTO> obtenerProductos(String nombreCategoria, Boolean disponibilidad) {
        List<Producto> productos;
        boolean tieneCategoria = nombreCategoria != null && !nombreCategoria.isBlank();

        if (tieneCategoria && disponibilidad != null) {
            productos = productoRepository.findByCategoriaAndDisponibilidad(nombreCategoria.trim(), disponibilidad);
        } else if (tieneCategoria) {
            productos = productoRepository.findByCategoria(nombreCategoria.trim());
        } else if (disponibilidad != null) {
            productos = productoRepository.findByDisponibilidad(disponibilidad);
        } else {
            productos = productoRepository.findAll();
        }

        return productoMapper.toResponseDTOList(productos);
    }

    @Override
    public ProductoResponseDTO buscarPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
        return productoMapper.toResponseDTO(producto);
    }

    @Override
    public ProductoResponseDTO crear(ProductoRequestDTO requestDTO) {
        Categoria categoria = categoriaRepository.findByNombreCategoria(requestDTO.getNombreCategoria())
                .orElseThrow(() -> new CategoriaNotFoundException(
                        String.format("No se encontró la categoría: %s", requestDTO.getNombreCategoria())
                ));

        String trimmedNombre = requestDTO.getNombre().trim();

        if (productoRepository.existsByNombre(trimmedNombre)) {
            throw new ProductoAlreadyExistsException(
                    String.format("Ya existe un producto registrado con el nombre: '%s'", trimmedNombre));
        }

        Producto producto = productoMapper.toEntity(requestDTO);
        producto.setNombre(trimmedNombre);
        producto.setNombreCategoria(categoria.getNombreCategoria());
        if (producto.getDescripcion() != null) {
            producto.setDescripcion(producto.getDescripcion().trim());
        }
        if (producto.getImagenUrl() != null) {
            producto.setImagenUrl(producto.getImagenUrl().trim());
        }

        Producto guardado = productoRepository.save(producto);
        return productoMapper.toResponseDTO(guardado);
    }

    @Override
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO requestDTO) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));

        Categoria categoria = categoriaRepository.findByNombreCategoria(requestDTO.getNombreCategoria())
                .orElseThrow(() -> new CategoriaNotFoundException(
                        String.format("No se encontró la categoría: %s", requestDTO.getNombreCategoria())
                ));

        String trimmedNombre = requestDTO.getNombre().trim();

        if (productoRepository.existsByNombreAndNotId(trimmedNombre, id)) {
            throw new ProductoAlreadyExistsException(
                    String.format("Ya existe otro producto registrado con el nombre: '%s'", trimmedNombre));
        }

        productoMapper.updateEntityFromDTO(requestDTO, productoExistente);
        productoExistente.setIdProducto(id);
        productoExistente.setNombre(trimmedNombre);
        productoExistente.setNombreCategoria(categoria.getNombreCategoria());
        if (productoExistente.getDescripcion() != null) {
            productoExistente.setDescripcion(productoExistente.getDescripcion().trim());
        }
        if (productoExistente.getImagenUrl() != null) {
            productoExistente.setImagenUrl(productoExistente.getImagenUrl().trim());
        }

        Producto actualizado = productoRepository.update(productoExistente);
        return productoMapper.toResponseDTO(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (productoRepository.findById(id).isEmpty()) {
            throw new ProductoNotFoundException(id);
        }
        productoRepository.deleteById(id);
    }

}
