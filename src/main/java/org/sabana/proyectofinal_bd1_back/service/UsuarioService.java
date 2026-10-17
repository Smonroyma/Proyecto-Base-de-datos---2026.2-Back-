package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para la gestión de usuarios.
 */
public interface UsuarioService {

    UsuarioResponseDTO buscarPorId(Long id);

    UsuarioResponseDTO buscarPorEmail(String email);

    List<UsuarioResponseDTO> obtenerTodos();

    UsuarioResponseDTO crear(UsuarioRequestDTO requestDTO);

    UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO requestDTO);

    void eliminar(Long id);

}
