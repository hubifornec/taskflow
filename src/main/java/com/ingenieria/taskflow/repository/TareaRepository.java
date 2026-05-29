package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;

/**
 * Repositorio JPA para la entidad {@link com.ingenieria.taskflow.model.Tarea}.
 * <p>
 * Extiende {@code JpaRepository} con consultas personalizadas para el tablero Kanban
 * y el sistema de gamificacion. Incluye una consulta JPQL para agrupacion por estado.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    /**
     * Obtiene todas las tareas de un usuario ordenadas de la mas reciente a la mas antigua.
     *
     * @param usuarioId ID del usuario propietario
     * @return lista de tareas ordenadas por {@code createdAt} descendente
     */
    List<Tarea> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);

    /**
     * Obtiene todas las tareas de un usuario sin orden especifico.
     *
     * @param usuarioId ID del usuario propietario
     * @return lista de tareas del usuario
     */
    List<Tarea> findByUsuarioId(Long usuarioId);

    /**
     * Obtiene las tareas de un usuario filtradas por estado.
     * Usado en gamificacion para contar tareas completadas.
     *
     * @param usuarioId ID del usuario propietario
     * @param estado    estado a filtrar: {@code "pendiente"}, {@code "en_proceso"}, {@code "completada"}
     * @return lista de tareas en el estado indicado
     */
    List<Tarea> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    /**
     * Cuenta el total de tareas de un usuario.
     * Usado en gamificacion para detectar la primera tarea creada.
     *
     * @param usuarioId ID del usuario
     * @return numero total de tareas del usuario
     */
    long countByUsuarioId(Long usuarioId);

    /**
     * Cuenta las tareas de un usuario en un estado especifico.
     *
     * @param usuarioId ID del usuario
     * @param estado    estado a contar
     * @return numero de tareas en ese estado
     */
    long countByUsuarioIdAndEstado(Long usuarioId, String estado);

    /**
     * Agrupa y cuenta las tareas de un usuario por estado mediante JPQL.
     * Retorna una lista de mapas con las claves {@code "estado"} y {@code "total"}.
     * Usado por {@code TareaService.obtenerStats()} para el panel de estadisticas.
     *
     * @param usuarioId ID del usuario
     * @return lista de mapas con el conteo por cada estado existente
     */
    @Query("SELECT t.estado AS estado, COUNT(t) AS total FROM Tarea t WHERE t.usuarioId = :usuarioId GROUP BY t.estado")
    List<Map<String, Object>> countGroupByEstado(@Param("usuarioId") Long usuarioId);
}
