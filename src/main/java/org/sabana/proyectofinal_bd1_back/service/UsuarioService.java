package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {

    UsuarioResponseDTO buscarPorId(Long id);

    UsuarioResponseDTO buscarPorCorreo(String correo);

    default UsuarioResponseDTO buscarPorEmail(String email) {
        return buscarPorCorreo(email);
    }

    List<UsuarioResponseDTO> obtenerTodos();

    UsuarioResponseDTO crear(UsuarioRequestDTO requestDTO);

    UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO requestDTO);

    UsuarioResponseDTO cambiarEstado(Long id, String nuevoEstado);

    void eliminar(Long id);

}
