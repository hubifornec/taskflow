package com.ingenieria.taskflow.service;

import com.ingenieria.taskflow.model.Tarea;
import com.ingenieria.taskflow.repository.TareaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class TareaService {

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    @Lazy
    private GamificacionService gamificacionService;

    public List<Tarea> obtenerPorUsuario(Long usuarioId) {
        return tareaRepository.findByUsuarioId(usuarioId);
    }

    public Tarea crear(Tarea tarea) {
        Tarea nueva = tareaRepository.save(tarea);
        if (tarea.getUsuarioId() != null)
            gamificacionService.procesarActividad(tarea.getUsuarioId(), "TAREA_CREADA");
        return nueva;
    }

    public Tarea actualizarEstado(Long id, String nuevoEstado) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        tarea.setEstado(nuevoEstado);
        tarea = tareaRepository.save(tarea);
        if (nuevoEstado.equals("completada") && tarea.getUsuarioId() != null)
            gamificacionService.procesarActividad(tarea.getUsuarioId(), "TAREA_COMPLETADA");
        return tarea;
    }

    public Map<String, Object> obtenerStats(Long usuarioId) {
        // Una sola query GROUP BY en lugar de 4 queries separadas
        List<Map<String, Object>> grupos = tareaRepository.countGroupByEstado(usuarioId);
        long pendientes = 0, enProceso = 0, completadas = 0;
        for (Map<String, Object> fila : grupos) {
            String estado = (String) fila.get("estado");
            long cnt = ((Number) fila.get("total")).longValue();
            if ("pendiente".equals(estado))   pendientes = cnt;
            else if ("en_proceso".equals(estado)) enProceso = cnt;
            else if ("completada".equals(estado)) completadas = cnt;
        }
        long total = pendientes + enProceso + completadas;
        return Map.of(
                "total", total,
                "pendientes", pendientes,
                "enProceso", enProceso,
                "completadas", completadas
        );
    }


    public void eliminar(Long id) {
        tareaRepository.deleteById(id);
    }

    public Tarea editar(Long id, Tarea datos) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        tarea.setTitulo(datos.getTitulo());
        tarea.setDescripcion(datos.getDescripcion());
        tarea.setPrioridad(datos.getPrioridad());
        tarea.setFechaVencimiento(datos.getFechaVencimiento());
        return tareaRepository.save(tarea);
    }
}