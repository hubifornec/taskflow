package com.ingenieria.taskflow.service;

import com.ingenieria.taskflow.dto.LoginRequest;
import com.ingenieria.taskflow.dto.RegisterRequest;
import com.ingenieria.taskflow.dto.UsuarioResponse;
import com.ingenieria.taskflow.exception.BadRequestException;
import com.ingenieria.taskflow.exception.ConflictException;
import com.ingenieria.taskflow.model.Usuario;
import com.ingenieria.taskflow.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Pruebas unitarias")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GamificacionService gamificacionService;

    @InjectMocks
    private AuthService authService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // Inyectar passwordEncoder manualmente por ser @Bean externo
        try {
            var field = AuthService.class.getDeclaredField("passwordEncoder");
            field.setAccessible(true);
            field.set(authService, passwordEncoder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ─── REGISTRO ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Registro exitoso con datos válidos")
    void registrar_datosValidos_retornaUsuarioResponse() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Ana García");
        req.setEmail("ana@example.com");
        req.setPassword("Segura123!");

        when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponse result = authService.registrar(req);

        assertNotNull(result);
        assertEquals("Ana García", result.getNombre());
        assertEquals("ana@example.com", result.getEmail());
        assertEquals("basico", result.getNivel());
        assertEquals(0, result.getPuntos());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro falla si el email ya existe")
    void registrar_emailDuplicado_lanzaConflictException() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Pedro");
        req.setEmail("existente@example.com");
        req.setPassword("Valida1!");

        when(usuarioRepository.existsByEmail("existente@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.registrar(req));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registro falla con contraseña sin mayúscula")
    void registrar_passwordSinMayuscula_lanzaBadRequestException() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Luis");
        req.setEmail("luis@example.com");
        req.setPassword("sinmayuscula1!");

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.registrar(req));
        assertTrue(ex.getMessage().contains("mayúscula"));
    }

    @Test
    @DisplayName("Registro falla con contraseña muy corta")
    void registrar_passwordCorta_lanzaBadRequestException() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Luis");
        req.setEmail("luis@example.com");
        req.setPassword("Ab1!");

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.registrar(req));
        assertTrue(ex.getMessage().contains("8 caracteres"));
    }

    @Test
    @DisplayName("Registro falla con contraseña sin número")
    void registrar_passwordSinNumero_lanzaBadRequestException() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Luis");
        req.setEmail("luis@example.com");
        req.setPassword("SinNumeros!");

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.registrar(req));
        assertTrue(ex.getMessage().contains("número"));
    }

    @Test
    @DisplayName("Registro falla con contraseña sin carácter especial")
    void registrar_passwordSinEspecial_lanzaBadRequestException() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Luis");
        req.setEmail("luis@example.com");
        req.setPassword("SinEspecial1");

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.registrar(req));
        assertTrue(ex.getMessage().contains("especial"));
    }

    @Test
    @DisplayName("La contraseña se almacena hasheada con BCrypt")
    void registrar_passwordSeHashea() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Test");
        req.setEmail("test@example.com");
        req.setPassword("Valida1!");

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            // La contraseña nunca debe guardarse en texto plano
            assertNotEquals("Valida1!", u.getPassword());
            assertTrue(u.getPassword().startsWith("$2a$"));
            return u;
        });

        authService.registrar(req);
    }

    // ─── LOGIN ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Login exitoso con credenciales correctas")
    void login_credencialesCorrectas_retornaUsuarioResponse() {
        String rawPassword = "Correcto1!";
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("user@example.com");
        usuario.setNombre("Usuario Test");
        usuario.setPassword(passwordEncoder.encode(rawPassword));
        usuario.setNivel("basico");
        usuario.setPuntos(100);
        usuario.setQuizzesCompletados(2);

        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword(rawPassword);

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));

        UsuarioResponse result = authService.login(req);

        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
        assertEquals(100, result.getPuntos());
    }

    @Test
    @DisplayName("Login falla con email inexistente")
    void login_emailNoExiste_lanzaBadRequestException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("noexiste@example.com");
        req.setPassword("Cualquier1!");

        when(usuarioRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.login(req));
    }

    @Test
    @DisplayName("Login falla con contraseña incorrecta")
    void login_passwordIncorrecta_lanzaBadRequestException() {
        Usuario usuario = new Usuario();
        usuario.setEmail("user@example.com");
        usuario.setPassword(passwordEncoder.encode("PasswordCorrecto1!"));

        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("PasswordIncorrecto1!");

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));

        assertThrows(BadRequestException.class, () -> authService.login(req));
    }

    @Test
    @DisplayName("Login normaliza email a minúsculas")
    void login_emailMayusculas_seNormaliza() {
        String rawPassword = "Test1234!";
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("user@example.com");
        usuario.setPassword(passwordEncoder.encode(rawPassword));
        usuario.setNivel("basico");
        usuario.setPuntos(0);
        usuario.setQuizzesCompletados(0);

        LoginRequest req = new LoginRequest();
        req.setEmail("USER@EXAMPLE.COM");
        req.setPassword(rawPassword);

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));

        UsuarioResponse result = authService.login(req);
        assertNotNull(result);
    }
}
