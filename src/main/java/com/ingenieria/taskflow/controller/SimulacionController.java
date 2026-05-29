package com.ingenieria.taskflow.controller;

import com.ingenieria.taskflow.service.SimulacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Controlador REST del modulo de simulacion de sprint planning de TaskFlow.
 * <p>
 * Gestiona el ciclo completo de una simulacion: inicio, consulta, seleccion
 * de items para el sprint, completado de items y cierre del sprint.
 * Base: {@code /api/simulacion}
 * </p>
 * <ul>
 *   <li>{@code POST /api/simulacion/iniciar} — Iniciar nueva simulacion</li>
 *   <li>{@code GET  /api/simulacion/activa/{usuarioId}} — Consultar simulacion activa</li>
 *   <li>{@code PATCH /api/simulacion/item/{id}/toggle} — Incluir/excluir item del sprint</li>
 *   <li>{@code PATCH /api/simulacion/item/{id}/completar} — Marcar item como completado</li>
 *   <li>{@code POST /api/simulacion/cerrar/{id}} — Cerrar sprint y obtener resultados</li>
 * </ul>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
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
