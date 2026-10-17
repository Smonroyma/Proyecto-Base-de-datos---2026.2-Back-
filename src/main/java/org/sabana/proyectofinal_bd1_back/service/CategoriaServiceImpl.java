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
    public List<CategoriaResponseDTO> obtenerCategorias(Boolean soloActivas) {
        List<Categoria> categorias;
        if (Boolean.TRUE.equals(soloActivas)) {
            categorias = categoriaRepository.findAllActivas();
        } else {
            categorias = categoriaRepository.findAll();
        }
        return categoriaMapper.toResponseDTOList(categorias);
    }

    @Override
    public CategoriaResponseDTO buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
        return categoriaMapper.toResponseDTO(categoria);
    }

    @Override
    public CategoriaResponseDTO crear(CategoriaRequestDTO requestDTO) {
        String trimmedNombre = requestDTO.getNombre().trim();

        if (categoriaRepository.existsByNombre(trimmedNombre)) {
            throw new CategoriaAlreadyExistsException(
                    String.format("Ya existe una categoría registrada con el nombre: '%s'", trimmedNombre));
        }

        Categoria categoria = categoriaMapper.toEntity(requestDTO);
        categoria.setNombre(trimmedNombre);
        if (categoria.getDescripcion() != null) {
            categoria.setDescripcion(categoria.getDescripcion().trim());
        }

        Categoria guardada = categoriaRepository.save(categoria);
        return categoriaMapper.toResponseDTO(guardada);
    }

    @Override
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO requestDTO) {
        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));

        String trimmedNombre = requestDTO.getNombre().trim();

        if (categoriaRepository.existsByNombreAndNotId(trimmedNombre, id)) {
            throw new CategoriaAlreadyExistsException(
                    String.format("Ya existe otra categoría registrada con el nombre: '%s'", trimmedNombre));
        }

        categoriaMapper.updateEntityFromDTO(requestDTO, categoriaExistente);
        categoriaExistente.setIdCategoria(id);
        categoriaExistente.setNombre(trimmedNombre);
        if (categoriaExistente.getDescripcion() != null) {
            categoriaExistente.setDescripcion(categoriaExistente.getDescripcion().trim());
        }

        Categoria actualizada = categoriaRepository.update(categoriaExistente);
        return categoriaMapper.toResponseDTO(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        if (categoriaRepository.findById(id).isEmpty()) {
            throw new CategoriaNotFoundException(id);
        }
        categoriaRepository.deleteById(id);
    }

}
