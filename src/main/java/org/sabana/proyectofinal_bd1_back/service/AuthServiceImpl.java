package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.AuthResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.LoginRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.Verificar2FARequestDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Usuario;
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

/**
 * Implementación de la lógica de autenticación JWT y verificación en dos pasos (2FA).
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final int OTP_VIGENCIA_MINUTOS = 5;

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
                .mensaje("Usuario registrado exitosamente en el sistema.")
                .usuario(nuevoUsuario)
                .build();
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO requestDTO) {
        Usuario usuario = usuarioRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas: correo o contraseña incorrectos."));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new InvalidCredentialsException("El usuario se encuentra inactivo. Comunícate con administración.");
        }

        if (!passwordEncoder.matches(requestDTO.getPassword(), usuario.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales inválidas: correo o contraseña incorrectos.");
        }

        UsuarioResponseDTO usuarioDTO = usuarioMapper.toResponseDTO(usuario);

        // Flujo con 2FA habilitado
        if (Boolean.TRUE.equals(usuario.getDosFactoresHabilitado())) {
            String codigoOtp = String.format("%06d", secureRandom.nextInt(1_000_000));
            LocalDateTime expiracion = LocalDateTime.now().plusMinutes(OTP_VIGENCIA_MINUTOS);

            usuarioRepository.guardarCodigoOTP(usuario.getIdUsuario(), codigoOtp, expiracion);
            emailService.sendOtpEmail(usuario.getEmail(), usuario.getNombre(), codigoOtp, OTP_VIGENCIA_MINUTOS);

            return AuthResponseDTO.builder()
                    .token(null)
                    .tipoToken(null)
                    .requiere2FA(true)
                    .mensaje(String.format("Se ha enviado un código de verificación (OTP) a su correo %s. Válido por %d minutos.",
                            usuario.getEmail(), OTP_VIGENCIA_MINUTOS))
                    .usuario(usuarioDTO)
                    .build();
        }

        // Flujo directo sin 2FA
        String token = jwtTokenProvider.generateToken(usuario);
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
        Usuario usuario = usuarioRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado para verificación."));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new InvalidCredentialsException("El usuario se encuentra inactivo.");
        }

        String otpAlmacenado = usuario.getCodigoOtp();
        LocalDateTime expiracion = usuario.getExpiracionOtp();

        if (otpAlmacenado == null || !otpAlmacenado.equals(requestDTO.getCodigoOtp().trim())) {
            throw new InvalidOtpException("El código de verificación proporcionado es incorrecto.");
        }

        if (expiracion == null || LocalDateTime.now().isAfter(expiracion)) {
            throw new OtpExpiredException("El código de verificación ha expirado. Por favor, solicita uno nuevo iniciando sesión.");
        }

        // OTP válido: Limpiar en BD y emitir token JWT
        usuarioRepository.limpiarCodigoOTP(usuario.getIdUsuario());
        String token = jwtTokenProvider.generateToken(usuario);

        return AuthResponseDTO.builder()
                .token(token)
                .tipoToken("Bearer")
                .requiere2FA(false)
                .mensaje("Verificación en dos pasos exitosa. Acceso concedido.")
                .usuario(usuarioMapper.toResponseDTO(usuario))
                .build();
    }

}
