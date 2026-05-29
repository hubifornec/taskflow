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

/**
 * Servicio de logica de negocio para la gestion de tareas del tablero Kanban.
 * <p>
 * Controla el ciclo de vida completo de una tarea: creacion, actualizacion de estado,
 * edicion, eliminacion y obtencion de estadisticas. Dispara eventos de gamificacion
 * al crear ({@code TAREA_CREADA}) y completar tareas ({@code TAREA_COMPLETADA}).
 * Solo acepta los estados: {@code "pendiente"}, {@code "en_proceso"}, {@code "completada"}.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Service
public class TareaService {

    private static final Logger log = LoggerFactory.getLogger(TareaService.class);

    private static final List<String> ESTADOS_VALIDOS = List.of("pendiente", "en_proceso", "completada");

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    @Lazy
    private GamificacionService gamificacionService;

    /**
     * Obtiene todas las tareas de un usuario ordenadas de la mas reciente a la mas antigua.
     *
     * @param usuarioId ID del usuario propietario de las tareas
     * @return lista de tareas ordenadas por fecha de creacion descendente
     */
    public List<Tarea> obtenerPorUsuario(Long usuarioId) {
        return tareaRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId);
    }

    /**
     * Crea una nueva tarea en el tablero Kanban y otorga puntos de gamificacion.
     * Dispara el evento {@code TAREA_CREADA} que suma 10 puntos al usuario.
     *
     * @param request DTO con los datos de la nueva tarea
     * @return la entidad {@code Tarea} guardada con su ID asignado
     */
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

    /**
     * Actualiza el estado de una tarea. Si el nuevo estado es {@code "completada"},
     * dispara el evento {@code TAREA_COMPLETADA} que suma 20 puntos al usuario.
     *
     * @param id          ID de la tarea a actualizar
     * @param nuevoEstado nuevo estado: {@code "pendiente"}, {@code "en_proceso"} o {@code "completada"}
     * @return la entidad {@code Tarea} con el estado actualizado
     * @throws BadRequestException si el estado no es uno de los valores validos
     * @throws ResourceNotFoundException si la tarea no existe
     */
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

    /**
     * Calcula las estadisticas de tareas de un usuario agrupadas por estado.
     *
     * @param usuarioId ID del usuario
     * @return mapa con claves: {@code total}, {@code pendientes}, {@code enProceso}, {@code completadas}
     */
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

    /**
     * Elimina una tarea de la base de datos verificando su existencia previa.
     *
     * @param id ID de la tarea a eliminar
     * @throws ResourceNotFoundException si la tarea no existe
     */
    @Transactional
    public void eliminar(Long id) {
        if (!tareaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tarea no encontrada");
        }
        tareaRepository.deleteById(id);
        log.info("Tarea eliminada id={}", id);
    }

    /**
     * Edita los campos de una tarea existente.
     *
     * @param id    ID de la tarea a editar
     * @param datos DTO con los nuevos valores
     * @return la entidad {@code Tarea} actualizada
     * @throws ResourceNotFoundException si la tarea no existe
     */
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
