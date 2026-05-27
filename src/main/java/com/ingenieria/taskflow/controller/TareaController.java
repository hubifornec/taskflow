package com.ingenieria.taskflow.controller;

import com.ingenieria.taskflow.dto.TareaRequest;
import com.ingenieria.taskflow.model.Tarea;
import com.ingenieria.taskflow.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
public class TareaController {

    @Autowired
    private TareaService tareaService;

    @GetMapping("/tareas")
    public ResponseEntity<List<Tarea>> obtenerTareas(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(tareaService.obtenerPorUsuario(usuarioId));
    }

    @PostMapping("/tareas")
    public ResponseEntity<Tarea> crearTarea(@Valid @RequestBody TareaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tareaService.crear(request));
    }

    @PatchMapping("/tareas/{id}/status")
    public ResponseEntity<Tarea> actualizarEstado(@PathVariable Long id,
                                                   @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(tareaService.actualizarEstado(id, body.get("estado")));
    }

    @GetMapping("/api/user/stats")
    public ResponseEntity<Map<String, Object>> obtenerStats(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(tareaService.obtenerStats(usuarioId));
    }

    @DeleteMapping("/tareas/{id}")
    public ResponseEntity<Map<String, String>> eliminarTarea(@PathVariable Long id) {
        tareaService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Tarea eliminada correctamente"));
    }

    @PutMapping("/tareas/{id}")
    public ResponseEntity<Tarea> editarTarea(@PathVariable Long id,
                                              @Valid @RequestBody TareaRequest request) {
        return ResponseEntity.ok(tareaService.editar(id, request));
    }
}
