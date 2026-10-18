package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.RolUsuario;
import org.sabana.proyectofinal_bd1_back.model.entity.Usuario;
import org.sabana.proyectofinal_bd1_back.repository.UsuarioRepository;
import org.sabana.proyectofinal_bd1_back.util.email.EmailService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.UserAlreadyExistsException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación de la lógica de negocio para administración de usuarios.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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

        if (usuarioGuardado != null && usuarioGuardado.getEmail() != null && !usuarioGuardado.getEmail().isBlank()) {
            Map<String, String> detalles = new LinkedHashMap<>();
            detalles.put("Nombre", usuarioGuardado.getNombre());
            detalles.put("Correo Electrónico", usuarioGuardado.getEmail());
            detalles.put("Rol Asignado", usuarioGuardado.getRol());
            detalles.put("Verificación 2FA", Boolean.TRUE.equals(usuarioGuardado.getDosFactoresHabilitado()) ? "Habilitada" : "Deshabilitada");
            detalles.put("Estado de Cuenta", "ACTIVA");

            emailService.sendNotificacionEmail(
                    usuarioGuardado.getEmail(),
                    "Bienvenido a Savana Coffee",
                    usuarioGuardado.getNombre(),
                    "Tu cuenta corporativa ha sido registrada exitosamente en la plataforma de gestión Savana Data Coffee.",
                    detalles,
                    "Recuerda mantener tus credenciales seguras y no compartir tu acceso con terceros."
            );
        }

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

        if (actualizado != null && !Boolean.TRUE.equals(actualizado.getActivo())) {
            enviarNotificacionDesactivacion(actualizado);
        }

        return usuarioMapper.toResponseDTO(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        usuarioRepository.deleteById(id);
        enviarNotificacionDesactivacion(existente);
    }

    private void enviarNotificacionDesactivacion(Usuario usuario) {
        if (usuario.getEmail() != null && !usuario.getEmail().isBlank()) {
            Map<String, String> detalles = new LinkedHashMap<>();
            detalles.put("Usuario", usuario.getNombre());
            detalles.put("Correo Electrónico", usuario.getEmail());
            detalles.put("Rol", usuario.getRol());
            detalles.put("Estado de Cuenta", "DESACTIVADA");
            detalles.put("Fecha de Operación", LocalDateTime.now().format(DATE_TIME_FORMATTER));

            emailService.sendNotificacionEmail(
                    usuario.getEmail(),
                    "Desactivación de Cuenta",
                    usuario.getNombre(),
                    "Te informamos que tu cuenta de acceso en el sistema Savana Data Coffee ha sido desactivada por la administración.",
                    detalles,
                    "Si consideras que se trata de un error, por favor comunícate con el administrador del sistema."
            );
        }
    }

    private void validarRol(String rol) {
        if (!RolUsuario.esValido(rol)) {
            throw new IllegalArgumentException(
                    String.format("El rol '%s' no es válido. Los roles permitidos son: ADMINISTRADOR, BARISTA, CAJERO", rol)
            );
        }
    }

}
