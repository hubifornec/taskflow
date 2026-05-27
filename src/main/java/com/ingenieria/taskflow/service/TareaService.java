package com.ingenieria.taskflow.service;

import com.ingenieria.taskflow.dto.TareaRequest;
import com.ingenieria.taskflow.exception.BadRequestException;
import com.ingenieria.taskflow.exception.ResourceNotFoundException;
import com.ingenieria.taskflow.model.Tarea;
import com.ingenieria.taskflow.repository.TareaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TareaService {

    private static final Logger log = LoggerFactory.getLogger(TareaService.class);

    private static final List<String> ESTADOS_VALIDOS = List.of("pendiente", "en_proceso", "completada");

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    @Lazy
    private GamificacionService gamificacionService;

    public List<Tarea> obtenerPorUsuario(Long usuarioId) {
        return tareaRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId);
    }

    @Transactional
    public Tarea crear(TareaRequest request) {
        Tarea tarea = new Tarea();
        tarea.setTitulo(request.getTitulo().trim());
        tarea.setDescripcion(request.getDescripcion());
        tarea.setUsuarioId(request.getUsuarioId());
        tarea.setPrioridad(request.getPrioridad() != null ? request.getPrioridad() : 5);
        tarea.setFechaVencimiento(request.getFechaVencimiento());
        tarea.setPuntosHistoria(request.getPuntosHistoria() != null ? request.getPuntosHistoria() : 3);
        tarea.setEstado("pendiente");
        tarea.setCreatedAt(LocalDateTime.now());

        Tarea nueva = tareaRepository.save(tarea);
        gamificacionService.procesarActividad(request.getUsuarioId(), "TAREA_CREADA");
        log.info("Tarea creada id={} usuario={}", nueva.getId(), nueva.getUsuarioId());
        return nueva;
    }

    @Transactional
    public Tarea actualizarEstado(Long id, String nuevoEstado) {
        if (!ESTADOS_VALIDOS.contains(nuevoEstado)) {
            throw new BadRequestException("Estado no válido: " + nuevoEstado);
        }
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
        tarea.setEstado(nuevoEstado);
        tarea = tareaRepository.save(tarea);

        if ("completada".equals(nuevoEstado) && tarea.getUsuarioId() != null) {
            gamificacionService.procesarActividad(tarea.getUsuarioId(), "TAREA_COMPLETADA");
        }
        return tarea;
    }

    public Map<String, Object> obtenerStats(Long usuarioId) {
        List<Map<String, Object>> grupos = tareaRepository.countGroupByEstado(usuarioId);
        long pendientes = 0, enProceso = 0, completadas = 0;
        for (Map<String, Object> fila : grupos) {
            String estado = (String) fila.get("estado");
            long cnt = ((Number) fila.get("total")).longValue();
            if ("pendiente".equals(estado))    pendientes = cnt;
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

    @Transactional
    public void eliminar(Long id) {
        if (!tareaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tarea no encontrada");
        }
        tareaRepository.deleteById(id);
        log.info("Tarea eliminada id={}", id);
    }

    @Transactional
    public Tarea editar(Long id, TareaRequest datos) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
        tarea.setTitulo(datos.getTitulo().trim());
        tarea.setDescripcion(datos.getDescripcion());
        if (datos.getPrioridad() != null) tarea.setPrioridad(datos.getPrioridad());
        if (datos.getFechaVencimiento() != null) tarea.setFechaVencimiento(datos.getFechaVencimiento());
        if (datos.getPuntosHistoria() != null) tarea.setPuntosHistoria(datos.getPuntosHistoria());
        return tareaRepository.save(tarea);
    }
}
