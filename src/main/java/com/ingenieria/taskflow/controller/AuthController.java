package com.ingenieria.taskflow.controller;

import com.ingenieria.taskflow.model.Usuario;
import com.ingenieria.taskflow.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/usuarios")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            Usuario nuevo = authService.registrar(usuario);
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        Usuario usuario = authService.login(email, password);
        if (usuario == null) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }
        return ResponseEntity.ok(usuario);
    }


    @PostMapping("/usuarios/{id}/completar-quiz")
    public ResponseEntity<?> completarQuiz(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(authService.completarQuiz(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}