package com.ingenieria.taskflow.controller;

import com.ingenieria.taskflow.service.SimulacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/simulacion")
public class SimulacionController {

    @Autowired
    private SimulacionService simulacionService;

    @PostMapping("/iniciar")
    public ResponseEntity<?> iniciar(@RequestBody Map<String, Object> body) {
        try {
            Long usuarioId   = Long.parseLong(body.get("usuarioId").toString());
            String goal      = body.getOrDefault("metaSprint",
                             body.getOrDefault("sprintGoal", "Completar el Sprint")).toString();
            Integer velocidad = body.containsKey("velocidad") ?
                Integer.parseInt(body.get("velocidad").toString()) : 20;
            return ResponseEntity.ok(
                simulacionService.iniciarSimulacion(usuarioId, goal, velocidad));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/activa/{usuarioId}")
    public ResponseEntity<?> obtenerActiva(@PathVariable Long usuarioId) {
        try {
            return ResponseEntity.ok(simulacionService.obtenerSimulacionActiva(usuarioId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/item/{itemId}/toggle")
    public ResponseEntity<?> toggleItem(@PathVariable Long itemId) {
        try {
            return ResponseEntity.ok(simulacionService.toggleItemSprint(itemId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/item/{itemId}/completar")
    public ResponseEntity<?> completarItem(@PathVariable Long itemId) {
        try {
            return ResponseEntity.ok(simulacionService.completarItem(itemId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cerrar/{simulacionId}")
    public ResponseEntity<?> cerrarSprint(@PathVariable Long simulacionId) {
        try {
            return ResponseEntity.ok(simulacionService.cerrarSprint(simulacionId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
