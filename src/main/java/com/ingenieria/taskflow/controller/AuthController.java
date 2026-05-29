package com.ingenieria.taskflow.controller;

import com.ingenieria.taskflow.dto.LoginRequest;
import com.ingenieria.taskflow.dto.RegisterRequest;
import com.ingenieria.taskflow.dto.UsuarioResponse;
import com.ingenieria.taskflow.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone los endpoints de autenticacion de TaskFlow.
 * <p>
 * Gestiona el registro de nuevos usuarios, el inicio de sesion y el registro
 * de quizzes completados. Todas las respuestas retornan un {@code UsuarioResponse}
 * sin exponer la contrasena.
 * </p>
 * <ul>
 *   <li>{@code POST /usuarios} — Registrar nuevo usuario (HTTP 201)</li>
 *   <li>{@code POST /login} — Iniciar sesion (HTTP 200)</li>
 *   <li>{@code POST /usuarios/{id}/completar-quiz} — Registrar quiz completado</li>
 * </ul>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Registra un nuevo usuario. Retorna HTTP 201 con los datos del usuario creado.
     *
     * @param request datos de registro validados con {@code @Valid}
     * @return {@code 201 Created} con {@code UsuarioResponse}
     */
    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegisterRequest request) {
        UsuarioResponse response = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Autentica al usuario con email y contrasena. Retorna HTTP 200 con datos del usuario.
     *
     * @param request credenciales validadas con {@code @Valid}
     * @return {@code 200 OK} con {@code UsuarioResponse}
     */
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest request) {
        UsuarioResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Registra la finalizacion de un quiz y actualiza puntos y nivel del usuario.
     *
     * @param id ID del usuario que completo el quiz
     * @return {@code 200 OK} con {@code UsuarioResponse} actualizado
     */
    @PostMapping("/usuarios/{id}/completar-quiz")
    public ResponseEntity<UsuarioResponse> completarQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(authService.completarQuiz(id));
    }
}
