package com.ingenieria.taskflow.controller;

import com.ingenieria.taskflow.model.Tarea;
import com.ingenieria.taskflow.service.TareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class TareaController {

    @Autowired
    private TareaService tareaService;

    @GetMapping("/tareas")
    public ResponseEntity<?> obtenerTareas(@RequestParam Long usuarioId) {
        try {
            List<Tarea> tareas = tareaService.obtenerPorUsuario(usuarioId);
            return ResponseEntity.ok(tareas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/tareas")
    public ResponseEntity<?> crearTarea(@RequestBody Tarea tarea) {
        try {
            return ResponseEntity.ok(tareaService.crear(tarea));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/tareas/{id}/status")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(tareaService.actualizarEstado(id, body.get("estado")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/user/stats")
    public ResponseEntity<?> obtenerStats(@RequestParam Long usuarioId) {
        try {
            return ResponseEntity.ok(tareaService.obtenerStats(usuarioId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    

    @DeleteMapping("/tareas/{id}")
    public ResponseEntity<?> eliminarTarea(@PathVariable Long id) {
        try {
            tareaService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Tarea eliminada"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/tareas/{id}")
    public ResponseEntity<?> editarTarea(@PathVariable Long id, @RequestBody Tarea tarea) {
        try {
            return ResponseEntity.ok(tareaService.editar(id, tarea));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

