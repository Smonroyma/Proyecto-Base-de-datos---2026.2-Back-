package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Administrador;
import org.sabana.proyectofinal_bd1_back.model.entity.Mesero;
import org.sabana.proyectofinal_bd1_back.model.entity.RolUsuario;
import org.sabana.proyectofinal_bd1_back.model.entity.Staff;
import org.sabana.proyectofinal_bd1_back.repository.UsuarioRepository;
import org.sabana.proyectofinal_bd1_back.util.email.EmailService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.UserAlreadyExistsException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Staff staff = usuarioRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return usuarioMapper.toResponseDTO(staff);
    }

    @Override
    public UsuarioResponseDTO buscarPorCorreo(String correo) {
        Staff staff = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UserNotFoundException("No se encontró el usuario con correo: " + correo));
        return usuarioMapper.toResponseDTO(staff);
    }

    @Override
    public List<UsuarioResponseDTO> obtenerTodos() {
        List<Staff> staffList = usuarioRepository.findAll();
        return usuarioMapper.toResponseDTOList(staffList);
    }

    @Override
    public UsuarioResponseDTO crear(UsuarioRequestDTO requestDTO) {
        validarRol(requestDTO.getRol());

        if (usuarioRepository.existsByCorreo(requestDTO.getCorreo())) {
            throw new UserAlreadyExistsException(
                    String.format("Ya existe un usuario registrado con el correo: %s", requestDTO.getCorreo())
            );
        }

        Staff staff = usuarioMapper.toEntity(requestDTO);
        staff.setContrasena(passwordEncoder.encode(requestDTO.getContrasena()));
        staff.setRol(requestDTO.getRol().toUpperCase().trim());
        staff.setDosFacHab(Boolean.TRUE.equals(requestDTO.getDosFacHab()));
        staff.setFechaInicio(LocalDate.now());

        if (staff instanceof Mesero mesero) {
            String estado = (requestDTO.getEstado() != null && !requestDTO.getEstado().isBlank())
                    ? requestDTO.getEstado().trim().toUpperCase()
                    : "ACTIVO";
            mesero.setEstado(estado);
        }

        Staff staffGuardado = usuarioRepository.save(staff);

        if (staffGuardado != null && staffGuardado.getCorreo() != null && !staffGuardado.getCorreo().isBlank()) {
            String estadoCuenta = (staffGuardado instanceof Mesero emp) ? emp.getEstado() : "ACTIVO";
            Map<String, String> detalles = new LinkedHashMap<>();
            detalles.put("Nombre", staffGuardado.getNombreCompleto());
            detalles.put("Correo Electrónico", staffGuardado.getCorreo());
            detalles.put("Rol Asignado", staffGuardado.getRol());
            detalles.put("Verificación 2FA", Boolean.TRUE.equals(staffGuardado.getDosFacHab()) ? "Habilitada" : "Deshabilitada");
            detalles.put("Estado de Cuenta", estadoCuenta);

            emailService.sendNotificacionEmail(
                    staffGuardado.getCorreo(),
                    "Bienvenido a Savana Coffee",
                    staffGuardado.getNombreCompleto(),
                    "Tu cuenta corporativa ha sido registrada exitosamente en la plataforma de gestión Savana Data Coffee.",
                    detalles,
                    "Recuerda mantener tus credenciales seguras y no compartir tu acceso con terceros."
            );
        }

        return usuarioMapper.toResponseDTO(staffGuardado);
    }

    @Override
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO requestDTO) {
        Staff existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        validarRol(requestDTO.getRol());

        if (usuarioRepository.existsByCorreoAndNotId(requestDTO.getCorreo(), id)) {
            throw new UserAlreadyExistsException(
                    String.format("Ya existe otro usuario registrado con el correo: %s", requestDTO.getCorreo())
            );
        }

        String rolNuevo = requestDTO.getRol().trim().toUpperCase();
        String contrasenaFinal = (requestDTO.getContrasena() != null && !requestDTO.getContrasena().isBlank())
                ? passwordEncoder.encode(requestDTO.getContrasena())
                : existente.getContrasena();
        Boolean dosFacFinal = requestDTO.getDosFacHab() != null
                ? requestDTO.getDosFacHab()
                : existente.getDosFacHab();
        String apellidosFinal = requestDTO.getApellidos() != null
                ? requestDTO.getApellidos().trim()
                : (existente.getApellidos() != null ? existente.getApellidos() : "");

        Staff entidadParaActualizar;
        if ("ADMINISTRADOR".equals(rolNuevo)) {
            entidadParaActualizar = Administrador.builder()
                    .idUsuario(id)
                    .nombres(requestDTO.getNombres().trim())
                    .apellidos(apellidosFinal)
                    .correo(requestDTO.getCorreo().trim().toLowerCase())
                    .contrasena(contrasenaFinal)
                    .dosFacHab(dosFacFinal)
                    .fechaInicio(existente.getFechaInicio())
                    .rol("ADMINISTRADOR")
                    .build();
        } else {
            String estadoMesero = "ACTIVO";
            if (requestDTO.getEstado() != null && !requestDTO.getEstado().isBlank()) {
                estadoMesero = requestDTO.getEstado().trim().toUpperCase();
            } else if (existente instanceof Mesero empExistente && empExistente.getEstado() != null) {
                estadoMesero = empExistente.getEstado();
            }
            entidadParaActualizar = Mesero.builder()
                    .idUsuario(id)
                    .nombres(requestDTO.getNombres().trim())
                    .apellidos(apellidosFinal)
                    .correo(requestDTO.getCorreo().trim().toLowerCase())
                    .contrasena(contrasenaFinal)
                    .dosFacHab(dosFacFinal)
                    .fechaInicio(existente.getFechaInicio())
                    .rol("MESERO")
                    .estado(estadoMesero)
                    .build();
        }

        Staff actualizado = usuarioRepository.update(entidadParaActualizar);

        if (actualizado instanceof Mesero emp && !emp.isActivo()) {
            enviarNotificacionDesactivacion(actualizado);
        }

        return usuarioMapper.toResponseDTO(actualizado);
    }

    @Override
    public UsuarioResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        Staff existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (nuevoEstado == null || (!"ACTIVO".equalsIgnoreCase(nuevoEstado.trim()) && !"INACTIVO".equalsIgnoreCase(nuevoEstado.trim()))) {
            throw new IllegalArgumentException("El estado del mesero debe ser ACTIVO o INACTIVO");
        }

        if (existente instanceof Administrador) {
            throw new IllegalArgumentException("El usuario con rol ADMINISTRADOR siempre permanece activo");
        }

        String estadoNormalizado = nuevoEstado.trim().toUpperCase();
        if (existente instanceof Mesero mesero) {
            mesero.setEstado(estadoNormalizado);
        }

        Staff actualizado = usuarioRepository.update(existente);
        if ("INACTIVO".equals(estadoNormalizado)) {
            enviarNotificacionDesactivacion(actualizado != null ? actualizado : existente);
        }

        return usuarioMapper.toResponseDTO(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        Staff existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        usuarioRepository.deleteById(id);
        enviarNotificacionDesactivacion(existente);
    }

    private void enviarNotificacionDesactivacion(Staff staff) {
        if (staff != null && staff.getCorreo() != null && !staff.getCorreo().isBlank()) {
            Map<String, String> detalles = new LinkedHashMap<>();
            detalles.put("Usuario", staff.getNombreCompleto());
            detalles.put("Correo Electrónico", staff.getCorreo());
            detalles.put("Rol", staff.getRol());
            detalles.put("Estado de Cuenta", "INACTIVO");
            detalles.put("Fecha de Operación", LocalDateTime.now().format(DATE_TIME_FORMATTER));

            emailService.sendNotificacionEmail(
                    staff.getCorreo(),
                    "Desactivación de Cuenta",
                    staff.getNombreCompleto(),
                    "Te informamos que tu cuenta de acceso en el sistema Savana Data Coffee ha sido desactivada por la administración.",
                    detalles,
                    "Si consideras que se trata de un error, por favor comunícate con el administrador del sistema."
            );
        }
    }

    private void validarRol(String rol) {
        if (!RolUsuario.esValido(rol)) {
            throw new IllegalArgumentException(
                    String.format("El rol '%s' no es válido. Los roles permitidos son: ADMINISTRADOR, MESERO", rol)
            );
        }
    }

}
