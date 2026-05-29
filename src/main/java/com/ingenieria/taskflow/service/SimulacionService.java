package com.ingenieria.taskflow.service;

import com.ingenieria.taskflow.model.*;
import com.ingenieria.taskflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio que gestiona la simulacion de sprint planning de TaskFlow.
 * <p>
 * Permite al estudiante practicar la planificacion de un sprint Scrum con
 * 12 historias de usuario predefinidas del mundo real. Calcula metricas
 * al cerrar el sprint: puntos planeados vs entregados, velocidad real,
 * exito (entregados >= planeados * 0.8) y genera una retrospectiva automatica.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Service
public class SimulacionService {

    @Autowired private SimulacionRepository simulacionRepository;
    @Autowired private SimulacionItemRepository simulacionItemRepository;

    private static final List<Map<String, Object>> PENDIENTES = List.of(
        Map.of("titulo", "Definir criterios de terminado del equipo",      "storyPoints", 3),
        Map.of("titulo", "Crear la lista de pendientes inicial",           "storyPoints", 5),
        Map.of("titulo", "Estimar historias con póker de planificación",   "storyPoints", 5),
        Map.of("titulo", "Configurar el tablero Kanban del equipo",        "storyPoints", 2),
        Map.of("titulo", "Realizar la planificación del Sprint",           "storyPoints", 3),
        Map.of("titulo", "Ejecutar reunión diaria durante el Sprint",      "storyPoints", 2),
        Map.of("titulo", "Desarrollar funcionalidad de inicio de sesión",  "storyPoints", 8),
        Map.of("titulo", "Desarrollar gestión de tareas",                  "storyPoints", 8),
        Map.of("titulo", "Implementar pruebas unitarias",                  "storyPoints", 5),
        Map.of("titulo", "Realizar revisión del Sprint con interesados",   "storyPoints", 3),
        Map.of("titulo", "Realizar retrospectiva del Sprint",              "storyPoints", 2),
        Map.of("titulo", "Documentar el incremento entregado",             "storyPoints", 3)
    );

    /**
     * Inicia una nueva simulacion de sprint planning para el usuario.
     * Si existia una simulacion activa previa, la pasa a estado {@code "abandonado"}.
     * Crea automaticamente los 12 items de backlog predefinidos.
     *
     * @param usuarioId  ID del usuario que inicia la simulacion
     * @param sprintGoal objetivo del sprint definido por el usuario
     * @param velocidad  velocidad del equipo en story points (defecto: 20)
     * @return mapa con: {@code simulacion} y {@code items} (lista de 12 items del backlog)
     */
    public Map<String, Object> iniciarSimulacion(Long usuarioId, String sprintGoal, Integer velocidad) {
        simulacionRepository.findByUsuarioIdAndEstado(usuarioId, "activo")
            .ifPresent(s -> { s.setEstado("abandonado"); simulacionRepository.save(s); });

        Simulacion sim = new Simulacion();
        sim.setUsuarioId(usuarioId);
        sim.setEstado("activo");
        sim.setSprintGoal(sprintGoal);
        sim.setVelocidadEquipo(velocidad != null ? velocidad : 20);
        sim.setCreatedAt(LocalDateTime.now());
        sim = simulacionRepository.save(sim);

        int orden = 1;
        for (Map<String, Object> item : PENDIENTES) {
            SimulacionItem si = new SimulacionItem();
            si.setSimulacionId(sim.getId());
            si.setTitulo(item.get("titulo").toString());
            si.setStoryPoints((Integer) item.get("storyPoints"));
            si.setEnSprint(false);
            si.setCompletado(false);
            si.setOrden(orden++);
            simulacionItemRepository.save(si);
        }

        return Map.of("simulacion", sim,
            "items", simulacionItemRepository.findBySimulacionId(sim.getId()));
    }

    /**
     * Consulta la simulacion activa del usuario si existe.
     *
     * @param usuarioId ID del usuario
     * @return mapa con {@code activa: false} si no hay simulacion, o
     *         {@code activa: true, simulacion, items} si la hay
     */
    public Map<String, Object> obtenerSimulacionActiva(Long usuarioId) {
        Optional<Simulacion> sim = simulacionRepository
            .findByUsuarioIdAndEstado(usuarioId, "activo");
        if (sim.isEmpty()) return Map.of("activa", false);
        return Map.of("activa", true, "simulacion", sim.get(),
            "items", simulacionItemRepository.findBySimulacionId(sim.get().getId()));
    }

    /**
     * Alterna el estado de un item entre incluido y excluido del sprint.
     *
     * @param itemId ID del item a alternar
     * @return el {@code SimulacionItem} con {@code enSprint} actualizado
     * @throws RuntimeException si el item no existe
     */
    public SimulacionItem toggleItemSprint(Long itemId) {
        SimulacionItem item = simulacionItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        item.setEnSprint(!item.getEnSprint());
        return simulacionItemRepository.save(item);
    }

    /**
     * Marca un item del sprint como completado (entregado).
     *
     * @param itemId ID del item a completar
     * @return el {@code SimulacionItem} con {@code completado = true}
     * @throws RuntimeException si el item no existe
     */
    public SimulacionItem completarItem(Long itemId) {
        SimulacionItem item = simulacionItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        item.setCompletado(true);
        return simulacionItemRepository.save(item);
    }

    /**
     * Cierra el sprint de una simulacion y calcula los resultados finales.
     * El sprint es exitoso si los puntos entregados son >= 80% de los planeados.
     *
     * @param simulacionId ID de la simulacion a cerrar
     * @return mapa con: {@code puntosPlaneados}, {@code puntosEntregados}, {@code velocidadReal},
     *         {@code exitoso}, {@code completados}, {@code total}, {@code retrospectiva}
     * @throws RuntimeException si la simulacion no existe
     */
    public Map<String, Object> cerrarSprint(Long simulacionId) {
        Simulacion sim = simulacionRepository.findById(simulacionId)
                .orElseThrow(() -> new RuntimeException("Simulación no encontrada"));

        List<SimulacionItem> enSprint = simulacionItemRepository
            .findBySimulacionIdAndEnSprint(simulacionId, true);
        List<SimulacionItem> completados = enSprint.stream()
            .filter(SimulacionItem::getCompletado).toList();

        int planeados  = enSprint.stream().mapToInt(SimulacionItem::getStoryPoints).sum();
        int entregados = completados.stream().mapToInt(SimulacionItem::getStoryPoints).sum();
        boolean exitoso = entregados >= (planeados * 0.8);

        sim.setEstado("cerrado");
        sim.setCerradoAt(LocalDateTime.now());
        simulacionRepository.save(sim);

        return Map.of(
            "puntosPlaneados",  planeados,
            "puntosEntregados", entregados,
            "velocidadReal",    entregados,
            "exitoso",          exitoso,
            "completados",      completados.size(),
            "total",            enSprint.size(),
            "retrospectiva",    generarRetrospectiva(exitoso, planeados, entregados)
        );
    }

    /**
     * Genera el texto de retrospectiva del sprint segun el resultado obtenido.
     *
     * @param exitoso    {@code true} si se alcanzo el 80% de los puntos planeados
     * @param planeados  story points totales planificados para el sprint
     * @param entregados story points efectivamente completados
     * @return texto de retroalimentacion para mostrar al estudiante
     */
    private String generarRetrospectiva(boolean exitoso, int planeados, int entregados) {
        if (exitoso)
            return "✅ ¡Sprint exitoso! Entregaste " + entregados + " de " + planeados +
                   " story points. El equipo cumplió el Sprint Goal. Para el próximo Sprint " +
                   "mantén la misma velocidad y mejora la calidad del código.";
        return "⚠️ Sprint incompleto. Solo entregaste " + entregados + " de " + planeados +
               " story points. Revisa si el equipo sobre-comprometió capacidad o si hubo " +
               "impedimentos. Considera reducir el alcance del próximo Sprint.";
    }
}
