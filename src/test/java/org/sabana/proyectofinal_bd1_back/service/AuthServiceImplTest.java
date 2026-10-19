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
import org.sabana.proyectofinal_bd1_back.model.entity.Administrador;
import org.sabana.proyectofinal_bd1_back.model.entity.Empleado;
import org.sabana.proyectofinal_bd1_back.repository.UsuarioRepository;
import org.sabana.proyectofinal_bd1_back.security.JwtTokenProvider;
import org.sabana.proyectofinal_bd1_back.util.email.EmailService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.InvalidCredentialsException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.InvalidOtpException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.OtpExpiredException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
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

    private Administrador adminSin2FA;
    private Empleado empleadoCon2FA;
    private UsuarioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        adminSin2FA = Administrador.builder()
                .idUsuario(1L)
                .nombres("Carlos")
                .apellidos("Mendoza")
                .correo("carlos@savanacoffee.com")
                .contrasena("hashedPassword123")
                .rol("ADMINISTRADOR")
                .dosFacHab(false)
                .fechaInicio(LocalDate.now())
                .build();

        empleadoCon2FA = Empleado.builder()
                .idUsuario(2L)
                .nombres("Mariana")
                .apellidos("Barista")
                .correo("mariana@savanacoffee.com")
                .contrasena("hashedPassword123")
                .rol("EMPLEADO")
                .estado("ACTIVO")
                .dosFacHab(true)
                .fechaInicio(LocalDate.now())
                .build();

        responseDTO = UsuarioResponseDTO.builder()
                .idUsuario(1L)
                .nombres("Carlos")
                .apellidos("Mendoza")
                .correo("carlos@savanacoffee.com")
                .rol("ADMINISTRADOR")
                .estado("ACTIVO")
                .dosFacHab(false)
                .build();
    }

    @Test
    @DisplayName("Login exitoso directo cuando 2FA está deshabilitado emite JWT de inmediato")
    void login_Sin2FA_ExitosoDirecto() {
        LoginRequestDTO request = new LoginRequestDTO("carlos@savanacoffee.com", "Password123*");

        when(usuarioRepository.findByCorreo("carlos@savanacoffee.com")).thenReturn(Optional.of(adminSin2FA));
        when(passwordEncoder.matches("Password123*", "hashedPassword123")).thenReturn(true);
        when(usuarioMapper.toResponseDTO(adminSin2FA)).thenReturn(responseDTO);
        when(jwtTokenProvider.generateToken(adminSin2FA)).thenReturn("mocked.jwt.token");

        AuthResponseDTO result = authService.login(request);

        assertNotNull(result);
        assertEquals("mocked.jwt.token", result.getToken());
        assertEquals("Bearer", result.getTipoToken());
        assertFalse(result.getRequiere2FA());
        verify(jwtTokenProvider).generateToken(adminSin2FA);
        verify(emailService, never()).sendOtpEmail(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Login paso 1 con 2FA habilitado almacena OTP en memoria ConcurrentHashMap y despacha correo HTML")
    void login_Con2FA_Paso1Exitoso_GeneraYEnviaOTP() {
        LoginRequestDTO request = new LoginRequestDTO("mariana@savanacoffee.com", "Password123*");

        when(usuarioRepository.findByCorreo("mariana@savanacoffee.com")).thenReturn(Optional.of(empleadoCon2FA));
        when(passwordEncoder.matches("Password123*", "hashedPassword123")).thenReturn(true);
        when(usuarioMapper.toResponseDTO(empleadoCon2FA)).thenReturn(responseDTO);

        AuthResponseDTO result = authService.login(request);

        assertNotNull(result);
        assertNull(result.getToken());
        assertTrue(result.getRequiere2FA());
        assertNotNull(authService.obtenerOtpEnMemoria("mariana@savanacoffee.com"));
        verify(emailService).sendOtpEmail(eq("mariana@savanacoffee.com"), eq("Mariana Barista"), anyString(), eq(5));
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Login con contraseña incorrecta lanza InvalidCredentialsException")
    void login_PasswordIncorrecto_LanzaInvalidCredentialsException() {
        LoginRequestDTO request = new LoginRequestDTO("carlos@savanacoffee.com", "WrongPassword");

        when(usuarioRepository.findByCorreo("carlos@savanacoffee.com")).thenReturn(Optional.of(adminSin2FA));
        when(passwordEncoder.matches("WrongPassword", "hashedPassword123")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Login con empleado en estado INACTIVO lanza InvalidCredentialsException")
    void login_EmpleadoInactivo_LanzaInvalidCredentialsException() {
        empleadoCon2FA.setEstado("INACTIVO");
        LoginRequestDTO request = new LoginRequestDTO("mariana@savanacoffee.com", "Password123*");

        when(usuarioRepository.findByCorreo("mariana@savanacoffee.com")).thenReturn(Optional.of(empleadoCon2FA));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Verificación 2FA con código OTP correcto y vigente en memoria emite JWT y limpia caché")
    void verificar2FA_Exitoso() {
        authService.guardarOtpEnMemoria("mariana@savanacoffee.com", "123456", LocalDateTime.now().plusMinutes(5));
        Verificar2FARequestDTO request = new Verificar2FARequestDTO("mariana@savanacoffee.com", "123456");

        when(usuarioRepository.findByCorreo("mariana@savanacoffee.com")).thenReturn(Optional.of(empleadoCon2FA));
        when(jwtTokenProvider.generateToken(empleadoCon2FA)).thenReturn("jwt.token.2fa");
        when(usuarioMapper.toResponseDTO(empleadoCon2FA)).thenReturn(responseDTO);

        AuthResponseDTO result = authService.verificar2FA(request);

        assertNotNull(result);
        assertEquals("jwt.token.2fa", result.getToken());
        assertFalse(result.getRequiere2FA());
        assertNull(authService.obtenerOtpEnMemoria("mariana@savanacoffee.com"));
        verify(jwtTokenProvider).generateToken(empleadoCon2FA);
    }

    @Test
    @DisplayName("Verificación 2FA con código OTP incorrecto lanza InvalidOtpException")
    void verificar2FA_CodigoIncorrecto_LanzaInvalidOtpException() {
        authService.guardarOtpEnMemoria("mariana@savanacoffee.com", "123456", LocalDateTime.now().plusMinutes(5));
        Verificar2FARequestDTO request = new Verificar2FARequestDTO("mariana@savanacoffee.com", "999999");

        when(usuarioRepository.findByCorreo("mariana@savanacoffee.com")).thenReturn(Optional.of(empleadoCon2FA));

        assertThrows(InvalidOtpException.class, () -> authService.verificar2FA(request));
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Verificación 2FA con código OTP expirado en memoria lanza OtpExpiredException")
    void verificar2FA_CodigoExpirado_LanzaOtpExpiredException() {
        authService.guardarOtpEnMemoria("mariana@savanacoffee.com", "123456", LocalDateTime.now().minusMinutes(1));
        Verificar2FARequestDTO request = new Verificar2FARequestDTO("mariana@savanacoffee.com", "123456");

        when(usuarioRepository.findByCorreo("mariana@savanacoffee.com")).thenReturn(Optional.of(empleadoCon2FA));

        assertThrows(OtpExpiredException.class, () -> authService.verificar2FA(request));
        assertNull(authService.obtenerOtpEnMemoria("mariana@savanacoffee.com"));
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Registro delega a UsuarioService y retorna AuthResponseDTO")
    void registrar_Exitoso() {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .nombres("Carlos")
                .apellidos("Mendoza")
                .correo("carlos@savanacoffee.com")
                .contrasena("Password123*")
                .rol("ADMINISTRADOR")
                .dosFacHab(false)
                .build();

        when(usuarioService.crear(request)).thenReturn(responseDTO);

        AuthResponseDTO result = authService.registrar(request);

        assertNotNull(result);
        assertNotNull(result.getUsuario());
        assertFalse(result.getRequiere2FA());
        verify(usuarioService).crear(request);
    }

}
