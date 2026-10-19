package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.CategoriaMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.CategoriaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.CategoriaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Categoria;
import org.sabana.proyectofinal_bd1_back.repository.CategoriaRepository;
import org.sabana.proyectofinal_bd1_back.util.exceptions.CategoriaAlreadyExistsException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.CategoriaNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de la lógica de negocio para la gestión de categorías.
 */
@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Override
    public List<CategoriaResponseDTO> obtenerCategorias(String estado) {
        List<Categoria> categorias;
        if (estado != null && !estado.isBlank()) {
            categorias = categoriaRepository.findByEstado(estado.toUpperCase().trim());
        } else {
            categorias = categoriaRepository.findAll();
        }
        return categoriaMapper.toResponseDTOList(categorias);
    }

    @Override
    public CategoriaResponseDTO buscarPorNombre(String nombreCategoria) {
        Categoria categoria = categoriaRepository.findByNombreCategoria(nombreCategoria)
                .orElseThrow(() -> new CategoriaNotFoundException(
                        String.format("No se encontró la categoría: %s", nombreCategoria)
                ));
        return categoriaMapper.toResponseDTO(categoria);
    }

    @Override
    public CategoriaResponseDTO crear(CategoriaRequestDTO requestDTO) {
        String trimmedNombre = requestDTO.getNombreCategoria().trim();

        if (categoriaRepository.existsByNombreCategoria(trimmedNombre)) {
            throw new CategoriaAlreadyExistsException(
                    String.format("Ya existe una categoría registrada con el nombre: '%s'", trimmedNombre));
        }

        Categoria categoria = categoriaMapper.toEntity(requestDTO);
        categoria.setNombreCategoria(trimmedNombre);
        categoria.setEstado(requestDTO.getEstado() != null ? requestDTO.getEstado().toUpperCase().trim() : "ACTIVA");
        if (categoria.getDescripcion() != null) {
            categoria.setDescripcion(categoria.getDescripcion().trim());
        }

        Categoria guardada = categoriaRepository.save(categoria);
        return categoriaMapper.toResponseDTO(guardada);
    }

    @Override
    public CategoriaResponseDTO actualizar(String nombreCategoria, CategoriaRequestDTO requestDTO) {
        Categoria categoriaExistente = categoriaRepository.findByNombreCategoria(nombreCategoria)
                .orElseThrow(() -> new CategoriaNotFoundException(
                        String.format("No se encontró la categoría: %s", nombreCategoria)
                ));

        String nuevoNombre = requestDTO.getNombreCategoria().trim();

        if (!categoriaExistente.getNombreCategoria().equalsIgnoreCase(nuevoNombre)
                && categoriaRepository.existsByNombreCategoria(nuevoNombre)) {
            throw new CategoriaAlreadyExistsException(
                    String.format("Ya existe otra categoría registrada con el nombre: '%s'", nuevoNombre));
        }

        String nombreOriginal = categoriaExistente.getNombreCategoria();
        categoriaMapper.updateEntityFromDTO(requestDTO, categoriaExistente);
        categoriaExistente.setNombreCategoria(nuevoNombre);
        categoriaExistente.setEstado(requestDTO.getEstado() != null ? requestDTO.getEstado().toUpperCase().trim() : "ACTIVA");
        if (categoriaExistente.getDescripcion() != null) {
            categoriaExistente.setDescripcion(categoriaExistente.getDescripcion().trim());
        }

        Categoria actualizada = categoriaRepository.update(nombreOriginal, categoriaExistente);
        return categoriaMapper.toResponseDTO(actualizada);
    }

    @Override
    public void eliminar(String nombreCategoria) {
        if (categoriaRepository.findByNombreCategoria(nombreCategoria).isEmpty()) {
            throw new CategoriaNotFoundException(
                    String.format("No se encontró la categoría: %s", nombreCategoria)
            );
        }
        categoriaRepository.deleteByNombreCategoria(nombreCategoria);
    }

}
