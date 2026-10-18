package org.sabana.proyectofinal_bd1_back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuarioSin2FA;
    private Usuario usuarioCon2FA;
    private UsuarioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        usuarioSin2FA = Usuario.builder()
                .idUsuario(1L)
                .nombre("Carlos Mendoza")
                .email("carlos@savanacoffee.com")
                .passwordHash("hashedPassword123")
                .rol("ADMINISTRADOR")
                .activo(true)
                .dosFactoresHabilitado(false)
                .fechaRegistro(LocalDateTime.now())
                .build();

        usuarioCon2FA = Usuario.builder()
                .idUsuario(2L)
                .nombre("Mariana Barista")
                .email("mariana@savanacoffee.com")
                .passwordHash("hashedPassword123")
                .rol("BARISTA")
                .activo(true)
                .dosFactoresHabilitado(true)
                .codigoOtp("123456")
                .expiracionOtp(LocalDateTime.now().plusMinutes(5))
                .fechaRegistro(LocalDateTime.now())
                .build();

        responseDTO = UsuarioResponseDTO.builder()
                .idUsuario(1L)
                .nombre("Carlos Mendoza")
                .email("carlos@savanacoffee.com")
                .rol("ADMINISTRADOR")
                .activo(true)
                .dosFactoresHabilitado(false)
                .build();
    }

    @Test
    @DisplayName("Login exitoso directo cuando 2FA está deshabilitado emite JWT de inmediato")
    void login_Sin2FA_ExitosoDirecto() {
        LoginRequestDTO request = new LoginRequestDTO("carlos@savanacoffee.com", "Password123*");

        when(usuarioRepository.findByEmail("carlos@savanacoffee.com")).thenReturn(Optional.of(usuarioSin2FA));
        when(passwordEncoder.matches("Password123*", "hashedPassword123")).thenReturn(true);
        when(usuarioMapper.toResponseDTO(usuarioSin2FA)).thenReturn(responseDTO);
        when(jwtTokenProvider.generateToken(usuarioSin2FA)).thenReturn("mocked.jwt.token");

        AuthResponseDTO result = authService.login(request);

        assertNotNull(result);
        assertEquals("mocked.jwt.token", result.getToken());
        assertEquals("Bearer", result.getTipoToken());
        assertFalse(result.getRequiere2FA());
        verify(jwtTokenProvider).generateToken(usuarioSin2FA);
        verify(emailService, never()).sendOtpEmail(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Login paso 1 con 2FA habilitado genera código OTP y despacha correo HTML")
    void login_Con2FA_Paso1Exitoso_GeneraYEnviaOTP() {
        LoginRequestDTO request = new LoginRequestDTO("mariana@savanacoffee.com", "Password123*");

        when(usuarioRepository.findByEmail("mariana@savanacoffee.com")).thenReturn(Optional.of(usuarioCon2FA));
        when(passwordEncoder.matches("Password123*", "hashedPassword123")).thenReturn(true);
        when(usuarioMapper.toResponseDTO(usuarioCon2FA)).thenReturn(responseDTO);

        AuthResponseDTO result = authService.login(request);

        assertNotNull(result);
        assertNull(result.getToken());
        assertTrue(result.getRequiere2FA());
        verify(usuarioRepository).guardarCodigoOTP(eq(2L), anyString(), any(LocalDateTime.class));
        verify(emailService).sendOtpEmail(eq("mariana@savanacoffee.com"), eq("Mariana Barista"), anyString(), eq(5));
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Login con contraseña incorrecta lanza InvalidCredentialsException")
    void login_PasswordIncorrecto_LanzaInvalidCredentialsException() {
        LoginRequestDTO request = new LoginRequestDTO("carlos@savanacoffee.com", "WrongPassword");

        when(usuarioRepository.findByEmail("carlos@savanacoffee.com")).thenReturn(Optional.of(usuarioSin2FA));
        when(passwordEncoder.matches("WrongPassword", "hashedPassword123")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Login con usuario inactivo lanza InvalidCredentialsException")
    void login_UsuarioInactivo_LanzaInvalidCredentialsException() {
        usuarioSin2FA.setActivo(false);
        LoginRequestDTO request = new LoginRequestDTO("carlos@savanacoffee.com", "Password123*");

        when(usuarioRepository.findByEmail("carlos@savanacoffee.com")).thenReturn(Optional.of(usuarioSin2FA));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Verificación 2FA con código OTP correcto y vigente emite JWT")
    void verificar2FA_Exitoso() {
        Verificar2FARequestDTO request = new Verificar2FARequestDTO("mariana@savanacoffee.com", "123456");

        when(usuarioRepository.findByEmail("mariana@savanacoffee.com")).thenReturn(Optional.of(usuarioCon2FA));
        when(jwtTokenProvider.generateToken(usuarioCon2FA)).thenReturn("jwt.token.2fa");
        when(usuarioMapper.toResponseDTO(usuarioCon2FA)).thenReturn(responseDTO);

        AuthResponseDTO result = authService.verificar2FA(request);

        assertNotNull(result);
        assertEquals("jwt.token.2fa", result.getToken());
        assertFalse(result.getRequiere2FA());
        verify(usuarioRepository).limpiarCodigoOTP(2L);
        verify(jwtTokenProvider).generateToken(usuarioCon2FA);
    }

    @Test
    @DisplayName("Verificación 2FA con código OTP incorrecto lanza InvalidOtpException")
    void verificar2FA_CodigoIncorrecto_LanzaInvalidOtpException() {
        Verificar2FARequestDTO request = new Verificar2FARequestDTO("mariana@savanacoffee.com", "999999");

        when(usuarioRepository.findByEmail("mariana@savanacoffee.com")).thenReturn(Optional.of(usuarioCon2FA));

        assertThrows(InvalidOtpException.class, () -> authService.verificar2FA(request));
        verify(usuarioRepository, never()).limpiarCodigoOTP(any());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Verificación 2FA con código OTP expirado lanza OtpExpiredException")
    void verificar2FA_CodigoExpirado_LanzaOtpExpiredException() {
        usuarioCon2FA.setExpiracionOtp(LocalDateTime.now().minusMinutes(1)); // Expirado
        Verificar2FARequestDTO request = new Verificar2FARequestDTO("mariana@savanacoffee.com", "123456");

        when(usuarioRepository.findByEmail("mariana@savanacoffee.com")).thenReturn(Optional.of(usuarioCon2FA));

        assertThrows(OtpExpiredException.class, () -> authService.verificar2FA(request));
        verify(usuarioRepository, never()).limpiarCodigoOTP(any());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Registro delega a UsuarioService y retorna AuthResponseDTO")
    void registrar_Exitoso() {
        UsuarioRequestDTO request = new UsuarioRequestDTO(
                "Carlos Mendoza", "carlos@savanacoffee.com", "Password123*", "ADMINISTRADOR", false
        );

        when(usuarioService.crear(request)).thenReturn(responseDTO);

        AuthResponseDTO result = authService.registrar(request);

        assertNotNull(result);
        assertNotNull(result.getUsuario());
        assertFalse(result.getRequiere2FA());
        verify(usuarioService).crear(request);
    }

}
