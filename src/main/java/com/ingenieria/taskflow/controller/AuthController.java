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

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegisterRequest request) {
        UsuarioResponse response = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest request) {
        UsuarioResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/usuarios/{id}/completar-quiz")
    public ResponseEntity<UsuarioResponse> completarQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(authService.completarQuiz(id));
    }
}
