package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.RolUsuario;
import org.sabana.proyectofinal_bd1_back.model.entity.Usuario;
import org.sabana.proyectofinal_bd1_back.repository.UsuarioRepository;
import org.sabana.proyectofinal_bd1_back.util.exceptions.UserAlreadyExistsException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de la lógica de negocio para administración de usuarios.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return usuarioMapper.toResponseDTO(usuario);
    }

    @Override
    public UsuarioResponseDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No se encontró el usuario con email: " + email));
        return usuarioMapper.toResponseDTO(usuario);
    }

    @Override
    public List<UsuarioResponseDTO> obtenerTodos() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarioMapper.toResponseDTOList(usuarios);
    }

    @Override
    public UsuarioResponseDTO crear(UsuarioRequestDTO requestDTO) {
        validarRol(requestDTO.getRol());

        if (usuarioRepository.existsByEmail(requestDTO.getEmail())) {
            throw new UserAlreadyExistsException(
                    String.format("Ya existe un usuario registrado con el correo: %s", requestDTO.getEmail())
            );
        }

        Usuario usuario = usuarioMapper.toEntity(requestDTO);
        usuario.setPasswordHash(passwordEncoder.encode(requestDTO.getPassword()));
        usuario.setRol(requestDTO.getRol().toUpperCase().trim());
        usuario.setActivo(true);
        usuario.setDosFactoresHabilitado(
                requestDTO.getDosFactoresHabilitado() != null ? requestDTO.getDosFactoresHabilitado() : false
        );
        usuario.setFechaRegistro(LocalDateTime.now());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(usuarioGuardado);
    }

    @Override
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO requestDTO) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        validarRol(requestDTO.getRol());

        if (usuarioRepository.existsByEmailAndNotId(requestDTO.getEmail(), id)) {
            throw new UserAlreadyExistsException(
                    String.format("Ya existe otro usuario registrado con el correo: %s", requestDTO.getEmail())
            );
        }

        usuarioMapper.updateEntityFromDTO(requestDTO, existente);
        existente.setIdUsuario(id);
        existente.setRol(requestDTO.getRol().toUpperCase().trim());

        if (requestDTO.getPassword() != null && !requestDTO.getPassword().isBlank()) {
            existente.setPasswordHash(passwordEncoder.encode(requestDTO.getPassword()));
        }

        if (requestDTO.getDosFactoresHabilitado() != null) {
            existente.setDosFactoresHabilitado(requestDTO.getDosFactoresHabilitado());
        }

        Usuario actualizado = usuarioRepository.update(existente);
        return usuarioMapper.toResponseDTO(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (usuarioRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException(id);
        }
        usuarioRepository.deleteById(id);
    }

    private void validarRol(String rol) {
        if (!RolUsuario.esValido(rol)) {
            throw new IllegalArgumentException(
                    String.format("El rol '%s' no es válido. Los roles permitidos son: ADMINISTRADOR, BARISTA, CAJERO", rol)
            );
        }
    }

}
