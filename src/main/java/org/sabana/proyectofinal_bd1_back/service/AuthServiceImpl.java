package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.AuthResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.LoginRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.Verificar2FARequestDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Empleado;
import org.sabana.proyectofinal_bd1_back.model.entity.Staff;
import org.sabana.proyectofinal_bd1_back.repository.UsuarioRepository;
import org.sabana.proyectofinal_bd1_back.security.JwtTokenProvider;
import org.sabana.proyectofinal_bd1_back.util.email.EmailService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.InvalidCredentialsException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.InvalidOtpException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.OtpExpiredException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación de la lógica de autenticación JWT y verificación en dos pasos (2FA) con caché OTP en memoria.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final int OTP_VIGENCIA_MINUTOS = 5;

    /**
     * Entrada inmutable para almacenar el código OTP y su fecha de expiración en memoria.
     */
    public record OtpEntry(String codigoOtp, LocalDateTime expiracion) {
    }

    private final Map<String, OtpEntry> otpCache = new ConcurrentHashMap<>();

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService,
            UsuarioMapper usuarioMapper,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
    }

    @Override
    public AuthResponseDTO registrar(UsuarioRequestDTO requestDTO) {
        UsuarioResponseDTO nuevoUsuario = usuarioService.crear(requestDTO);

        return AuthResponseDTO.builder()
                .requiere2FA(false)
                .mensaje("Miembro de staff registrado exitosamente en el sistema.")
                .usuario(nuevoUsuario)
                .build();
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO requestDTO) {
        Staff staff = usuarioRepository.findByCorreo(requestDTO.getCorreo())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas: correo o contraseña incorrectos."));

        if (staff instanceof Empleado empleado && !empleado.isActivo()) {
            throw new InvalidCredentialsException("El empleado se encuentra inactivo. Comunícate con administración.");
        }

        if (!passwordEncoder.matches(requestDTO.getContrasena(), staff.getContrasena())) {
            throw new InvalidCredentialsException("Credenciales inválidas: correo o contraseña incorrectos.");
        }

        UsuarioResponseDTO usuarioDTO = usuarioMapper.toResponseDTO(staff);

        // Flujo con 2FA habilitado: almacenar OTP en ConcurrentHashMap en memoria
        if (Boolean.TRUE.equals(staff.getDosFacHab())) {
            String codigoOtp = String.format("%06d", secureRandom.nextInt(1_000_000));
            LocalDateTime expiracion = LocalDateTime.now().plusMinutes(OTP_VIGENCIA_MINUTOS);
            String key = normalizarCorreoKey(staff.getCorreo());

            otpCache.put(key, new OtpEntry(codigoOtp, expiracion));
            emailService.sendOtpEmail(staff.getCorreo(), staff.getNombreCompleto(), codigoOtp, OTP_VIGENCIA_MINUTOS);

            return AuthResponseDTO.builder()
                    .token(null)
                    .tipoToken(null)
                    .requiere2FA(true)
                    .mensaje(String.format("Se ha enviado un código de verificación (OTP) a su correo %s. Válido por %d minutos.",
                            staff.getCorreo(), OTP_VIGENCIA_MINUTOS))
                    .usuario(usuarioDTO)
                    .build();
        }

        // Flujo directo sin 2FA
        String token = jwtTokenProvider.generateToken(staff);
        return AuthResponseDTO.builder()
                .token(token)
                .tipoToken("Bearer")
                .requiere2FA(false)
                .mensaje("Inicio de sesión exitoso.")
                .usuario(usuarioDTO)
                .build();
    }

    @Override
    public AuthResponseDTO verificar2FA(Verificar2FARequestDTO requestDTO) {
        Staff staff = usuarioRepository.findByCorreo(requestDTO.getCorreo())
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado para verificación."));

        if (staff instanceof Empleado empleado && !empleado.isActivo()) {
            throw new InvalidCredentialsException("El empleado se encuentra inactivo.");
        }

        String key = normalizarCorreoKey(staff.getCorreo());
        OtpEntry otpEntry = otpCache.get(key);

        if (otpEntry == null || otpEntry.codigoOtp() == null
                || !otpEntry.codigoOtp().equals(requestDTO.getCodigoOtp().trim())) {
            throw new InvalidOtpException("El código de verificación proporcionado es incorrecto.");
        }

        if (otpEntry.expiracion() == null || LocalDateTime.now().isAfter(otpEntry.expiracion())) {
            otpCache.remove(key);
            throw new OtpExpiredException("El código de verificación ha expirado. Por favor, solicita uno nuevo iniciando sesión.");
        }

        // OTP válido: remover de memoria y emitir token JWT
        otpCache.remove(key);
        String token = jwtTokenProvider.generateToken(staff);

        return AuthResponseDTO.builder()
                .token(token)
                .tipoToken("Bearer")
                .requiere2FA(false)
                .mensaje("Verificación en dos pasos exitosa. Acceso concedido.")
                .usuario(usuarioMapper.toResponseDTO(staff))
                .build();
    }

    /**
     * Registra manualmente un OTP en la caché en memoria (útil para pruebas unitarias).
     */
    public void guardarOtpEnMemoria(String correo, String codigoOtp, LocalDateTime expiracion) {
        otpCache.put(normalizarCorreoKey(correo), new OtpEntry(codigoOtp, expiracion));
    }

    /**
     * Obtiene la entrada OTP almacenada en memoria para un correo dado.
     */
    public OtpEntry obtenerOtpEnMemoria(String correo) {
        return otpCache.get(normalizarCorreoKey(correo));
    }

    private String normalizarCorreoKey(String correo) {
        return correo != null ? correo.trim().toLowerCase() : "";
    }

}
