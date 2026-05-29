package com.ingenieria.taskflow.controller;

import com.ingenieria.taskflow.service.GamificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Controlador REST del sistema de gamificacion de TaskFlow.
 * <p>
 * Permite procesar actividades manualmente y consultar los logros
 * globales del sistema o los obtenidos por un usuario especifico.
 * Base: {@code /api/gamificacion}
 * </p>
 * <ul>
 *   <li>{@code POST /api/gamificacion/actividad} — Procesar actividad (puntos y logros)</li>
 *   <li>{@code GET  /api/gamificacion/logros} — Todos los logros del sistema</li>
 *   <li>{@code GET  /api/gamificacion/logros/{usuarioId}} — Logros obtenidos por un usuario</li>
 * </ul>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/gamificacion")
public class GamificacionController {

    @Autowired
    private GamificacionService gamificacionService;

    @PostMapping("/actividad")
    public ResponseEntity<?> procesarActividad(@RequestBody Map<String, Object> body) {
        try {
            Long usuarioId = Long.parseLong(body.get("usuarioId").toString());
            String tipo    = body.get("tipo").toString();
            return ResponseEntity.ok(gamificacionService.procesarActividad(usuarioId, tipo));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/logros/{usuarioId}")
    public ResponseEntity<?> obtenerLogros(@PathVariable Long usuarioId) {
        try {
            return ResponseEntity.ok(gamificacionService.obtenerLogrosUsuario(usuarioId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/logros")
    public ResponseEntity<?> obtenerTodos() {
        try {
            return ResponseEntity.ok(gamificacionService.obtenerTodosLogros());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
