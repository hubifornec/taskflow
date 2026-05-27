package com.ingenieria.taskflow.service;

import com.ingenieria.taskflow.dto.LoginRequest;
import com.ingenieria.taskflow.dto.RegisterRequest;
import com.ingenieria.taskflow.dto.UsuarioResponse;
import com.ingenieria.taskflow.exception.BadRequestException;
import com.ingenieria.taskflow.exception.ConflictException;
import com.ingenieria.taskflow.exception.ResourceNotFoundException;
import com.ingenieria.taskflow.model.Usuario;
import com.ingenieria.taskflow.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Lazy
    private GamificacionService gamificacionService;

    @Transactional
    public UsuarioResponse registrar(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new ConflictException("Ya existe una cuenta con ese email");
        }
        validarPassword(request.getPassword());

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setEmail(request.getEmail().toLowerCase().trim());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setNivel("basico");
        usuario.setQuizzesCompletados(0);
        usuario.setPuntos(0);
        usuario.setCreatedAt(LocalDateTime.now());

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario registrado: {}", guardado.getEmail());
        return UsuarioResponse.from(guardado);
    }

    public UsuarioResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            log.warn("Intento de login fallido para: {}", request.getEmail());
            throw new BadRequestException("Credenciales incorrectas");
        }

        log.info("Login exitoso: {}", usuario.getEmail());
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public UsuarioResponse completarQuiz(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String nivelAntes = usuario.getNivel();
        usuario.setQuizzesCompletados(usuario.getQuizzesCompletados() + 1);

        if (usuario.getQuizzesCompletados() >= 4 && "basico".equals(usuario.getNivel())) {
            usuario.setNivel("avanzado");
        }

        usuario = usuarioRepository.save(usuario);
        gamificacionService.procesarActividad(id, "QUIZ_COMPLETADO");

        if (!nivelAntes.equals(usuario.getNivel())) {
            gamificacionService.procesarActividad(id, "NIVEL_AVANZADO");
        }

        return UsuarioResponse.from(usuario);
    }

    private void validarPassword(String password) {
        if (password == null || password.length() < 8)
            throw new BadRequestException("La contraseña debe tener mínimo 8 caracteres");
        if (password.chars().noneMatch(Character::isUpperCase))
            throw new BadRequestException("La contraseña debe tener al menos una mayúscula");
        if (password.chars().noneMatch(Character::isDigit))
            throw new BadRequestException("La contraseña debe tener al menos un número");
        if (password.chars().noneMatch(c -> "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0))
            throw new BadRequestException("La contraseña debe tener al menos un carácter especial");
    }
}
