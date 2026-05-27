package com.ingenieria.taskflow.service;

import com.ingenieria.taskflow.model.*;
import com.ingenieria.taskflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

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

    public Map<String, Object> obtenerSimulacionActiva(Long usuarioId) {
        Optional<Simulacion> sim = simulacionRepository
            .findByUsuarioIdAndEstado(usuarioId, "activo");
        if (sim.isEmpty()) return Map.of("activa", false);
        return Map.of("activa", true, "simulacion", sim.get(),
            "items", simulacionItemRepository.findBySimulacionId(sim.get().getId()));
    }

    public SimulacionItem toggleItemSprint(Long itemId) {
        SimulacionItem item = simulacionItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        item.setEnSprint(!item.getEnSprint());
        return simulacionItemRepository.save(item);
    }

    public SimulacionItem completarItem(Long itemId) {
        SimulacionItem item = simulacionItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        item.setCompletado(true);
        return simulacionItemRepository.save(item);
    }

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
