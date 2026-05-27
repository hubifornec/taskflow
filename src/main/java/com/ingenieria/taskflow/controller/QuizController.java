package com.ingenieria.taskflow.controller;

import com.ingenieria.taskflow.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.ingenieria.taskflow.model.Usuario;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class QuizController {

    @Autowired
    private QuizService quizService;
    @Autowired
    private com.ingenieria.taskflow.repository.UsuarioRepository usuarioRepository;


    @GetMapping("/quiz")
    public ResponseEntity<?> obtenerQuizzes() {
        try {
            return ResponseEntity.ok(quizService.obtenerTodos());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/quiz/nivel/{nivel}")
    public ResponseEntity<?> obtenerPorNivel(@PathVariable String nivel) {
        try {
            return ResponseEntity.ok(quizService.obtenerPorNivel(nivel));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/quiz/{cuestionarioId}")
    public ResponseEntity<?> obtenerPreguntas(@PathVariable Long cuestionarioId) {
        try {
            return ResponseEntity.ok(quizService.obtenerPreguntasPorCuestionario(cuestionarioId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/teoria/{cuestionarioId}")
    public ResponseEntity<?> obtenerTeoria(@PathVariable Long cuestionarioId) {
        try {
            return ResponseEntity.ok(quizService.obtenerTeoriaPorCuestionario(cuestionarioId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/quiz/answer")
    public ResponseEntity<?> validarRespuesta(@RequestBody Map<String, String> body) {
        try {
            Long preguntaId = Long.parseLong(body.get("preguntaId"));
            String respuesta = body.get("respuesta");
            return ResponseEntity.ok(quizService.validarRespuesta(preguntaId, respuesta));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/user/progress")
    public ResponseEntity<?> obtenerProgreso(@RequestParam Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            return ResponseEntity.ok(Map.of(
                    "nivel", usuario.getNivel(),
                    "quizzesCompletados", usuario.getQuizzesCompletados(),
                    "totalQuizzes", 8
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}