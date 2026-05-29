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

/**
 * Controlador REST que expone los endpoints del tablero Kanban de TaskFlow.
 * <p>
 * Permite crear, listar, editar, eliminar y cambiar el estado de las tareas.
 * Tambien expone estadisticas de tareas por usuario.
 * </p>
 * <ul>
 *   <li>{@code GET  /tareas?usuarioId=} — Listar tareas del usuario</li>
 *   <li>{@code POST /tareas} — Crear nueva tarea (HTTP 201)</li>
 *   <li>{@code PATCH /tareas/{id}/status} — Actualizar estado</li>
 *   <li>{@code PUT  /tareas/{id}} — Editar tarea completa</li>
 *   <li>{@code DELETE /tareas/{id}} — Eliminar tarea</li>
 *   <li>{@code GET  /api/user/stats?usuarioId=} — Estadisticas por estado</li>
 * </ul>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@RestController
public class TareaController {

    @Autowired
    private TareaService tareaService;

    /**
     * Lista todas las tareas del usuario especificado, ordenadas de mas reciente a mas antigua.
     *
     * @param usuarioId ID del usuario propietario de las tareas
     * @return {@code 200 OK} con lista de tareas
     */
    @GetMapping("/tareas")
    public ResponseEntity<List<Tarea>> obtenerTareas(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(tareaService.obtenerPorUsuario(usuarioId));
    }

    /**
     * Crea una nueva tarea en el tablero Kanban. Retorna HTTP 201.
     *
     * @param request datos de la tarea validados con {@code @Valid}
     * @return {@code 201 Created} con la tarea creada
     */
    @PostMapping("/tareas")
    public ResponseEntity<Tarea> crearTarea(@Valid @RequestBody TareaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tareaService.crear(request));
    }

    /**
     * Actualiza el estado de una tarea (pendiente, en_proceso, completada).
     *
     * @param id   ID de la tarea
     * @param body mapa con la clave {@code "estado"} y el nuevo valor
     * @return {@code 200 OK} con la tarea actualizada
     */
    @PatchMapping("/tareas/{id}/status")
    public ResponseEntity<Tarea> actualizarEstado(@PathVariable Long id,
                                                   @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(tareaService.actualizarEstado(id, body.get("estado")));
    }

    /**
     * Retorna estadisticas de tareas del usuario agrupadas por estado.
     *
     * @param usuarioId ID del usuario
     * @return {@code 200 OK} con total, pendientes, enProceso y completadas
     */
    @GetMapping("/api/user/stats")
    public ResponseEntity<Map<String, Object>> obtenerStats(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(tareaService.obtenerStats(usuarioId));
    }

    /**
     * Elimina una tarea por su ID.
     *
     * @param id ID de la tarea a eliminar
     * @return {@code 200 OK} con mensaje de confirmacion
     */
    @DeleteMapping("/tareas/{id}")
    public ResponseEntity<Map<String, String>> eliminarTarea(@PathVariable Long id) {
        tareaService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Tarea eliminada correctamente"));
    }

    /**
     * Edita completamente los campos de una tarea existente.
     *
     * @param id      ID de la tarea a editar
     * @param request nuevos datos validados con {@code @Valid}
     * @return {@code 200 OK} con la tarea editada
     */
    @PutMapping("/tareas/{id}")
    public ResponseEntity<Tarea> editarTarea(@PathVariable Long id,
                                              @Valid @RequestBody TareaRequest request) {
        return ResponseEntity.ok(tareaService.editar(id, request));
    }
}
