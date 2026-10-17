package org.sabana.proyectofinal_bd1_back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioMapper;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.entity.Usuario;
import org.sabana.proyectofinal_bd1_back.repository.UsuarioRepository;
import org.sabana.proyectofinal_bd1_back.util.exceptions.UserAlreadyExistsException;
import org.sabana.proyectofinal_bd1_back.util.exceptions.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;
    private UsuarioRequestDTO requestDTO;
    private UsuarioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .idUsuario(1L)
                .nombre("Sofía Monroy")
                .email("sofia@savanacoffee.com")
                .passwordHash("encodedHash")
                .rol("ADMINISTRADOR")
                .activo(true)
                .dosFactoresHabilitado(true)
                .fechaRegistro(LocalDateTime.now())
                .build();

        requestDTO = UsuarioRequestDTO.builder()
                .nombre("Sofía Monroy")
                .email("sofia@savanacoffee.com")
                .password("ClaveSegura2026")
                .rol("ADMINISTRADOR")
                .dosFactoresHabilitado(true)
                .build();

        responseDTO = UsuarioResponseDTO.builder()
                .idUsuario(1L)
                .nombre("Sofía Monroy")
                .email("sofia@savanacoffee.com")
                .rol("ADMINISTRADOR")
                .activo(true)
                .dosFactoresHabilitado(true)
                .fechaRegistro(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("buscarPorId retorna UsuarioResponseDTO cuando el usuario existe")
    void buscarPorId_Exitoso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(responseDTO);

        UsuarioResponseDTO result = usuarioService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdUsuario());
        assertEquals("sofia@savanacoffee.com", result.getEmail());
        verify(usuarioRepository).findById(1L);
    }

    @Test
    @DisplayName("buscarPorId lanza UserNotFoundException cuando no existe")
    void buscarPorId_NoExiste_LanzaUserNotFoundException() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> usuarioService.buscarPorId(99L));
        verify(usuarioRepository).findById(99L);
    }

    @Test
    @DisplayName("buscarPorEmail retorna UsuarioResponseDTO cuando el usuario existe")
    void buscarPorEmail_Exitoso() {
        when(usuarioRepository.findByEmail("sofia@savanacoffee.com")).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(responseDTO);

        UsuarioResponseDTO result = usuarioService.buscarPorEmail("sofia@savanacoffee.com");

        assertNotNull(result);
        assertEquals("sofia@savanacoffee.com", result.getEmail());
        verify(usuarioRepository).findByEmail("sofia@savanacoffee.com");
    }

    @Test
    @DisplayName("crear registra usuario con contraseña encriptada por BCrypt")
    void crear_Exitoso() {
        when(usuarioRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(usuarioMapper.toEntity(requestDTO)).thenReturn(usuario);
        when(passwordEncoder.encode(requestDTO.getPassword())).thenReturn("encodedHash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(responseDTO);

        UsuarioResponseDTO result = usuarioService.crear(requestDTO);

        assertNotNull(result);
        assertEquals("sofia@savanacoffee.com", result.getEmail());
        verify(passwordEncoder).encode("ClaveSegura2026");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("crear lanza UserAlreadyExistsException cuando el correo ya está registrado")
    void crear_EmailDuplicado_LanzaUserAlreadyExistsException() {
        when(usuarioRepository.existsByEmail(requestDTO.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> usuarioService.crear(requestDTO));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("crear lanza IllegalArgumentException cuando el rol no es válido")
    void crear_RolInvalido_LanzaIllegalArgumentException() {
        requestDTO.setRol("SUPER_USUARIO_INVALIDO");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.crear(requestDTO));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("actualizar modifica datos del usuario correctamente")
    void actualizar_Exitoso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailAndNotId(requestDTO.getEmail(), 1L)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedHash");
        when(usuarioRepository.update(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(responseDTO);

        UsuarioResponseDTO result = usuarioService.actualizar(1L, requestDTO);

        assertNotNull(result);
        verify(usuarioRepository).update(usuario);
    }

    @Test
    @DisplayName("actualizar lanza UserAlreadyExistsException si otro usuario ya usa el correo")
    void actualizar_EmailDuplicado_LanzaUserAlreadyExistsException() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailAndNotId(requestDTO.getEmail(), 1L)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> usuarioService.actualizar(1L, requestDTO));
        verify(usuarioRepository, never()).update(any(Usuario.class));
    }

    @Test
    @DisplayName("eliminar borra el usuario cuando existe")
    void eliminar_Exitoso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.eliminar(1L);

        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar lanza UserNotFoundException si el usuario no existe")
    void eliminar_NoExiste_LanzaUserNotFoundException() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> usuarioService.eliminar(99L));
        verify(usuarioRepository, never()).deleteById(99L);
    }

}
