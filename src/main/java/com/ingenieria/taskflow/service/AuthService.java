package com.ingenieria.taskflow.service;

import com.ingenieria.taskflow.model.Usuario;
import com.ingenieria.taskflow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    @Lazy
    private GamificacionService gamificacionService;

    public Usuario registrar(Usuario usuario) {
        if (!validarPassword(usuario.getPassword())) {
            throw new RuntimeException("La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial");
        }
        usuario.setNivel("basico");
        usuario.setQuizzesCompletados(0);
        return usuarioRepository.save(usuario);
    }

    public Usuario login(String email, String password) {
        return usuarioRepository.findByEmailAndPassword(email, password)
                .orElse(null);
    }

    public Usuario completarQuiz(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        String nivelAntes = usuario.getNivel();
        usuario.setQuizzesCompletados(usuario.getQuizzesCompletados() + 1);
        if (usuario.getQuizzesCompletados() >= 4 && usuario.getNivel().equals("basico")) {
            usuario.setNivel("avanzado");
        }
        usuario = usuarioRepository.save(usuario);
        gamificacionService.procesarActividad(id, "QUIZ_COMPLETADO");
        if (!nivelAntes.equals(usuario.getNivel())) {
            gamificacionService.procesarActividad(id, "NIVEL_AVANZADO");
        }
        return usuario;
    }

    private boolean validarPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean tieneMayuscula = password.chars().anyMatch(Character::isUpperCase);
        boolean tieneNumero = password.chars().anyMatch(Character::isDigit);
        boolean tieneEspecial = password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0);
        return tieneMayuscula && tieneNumero && tieneEspecial;
    }
}