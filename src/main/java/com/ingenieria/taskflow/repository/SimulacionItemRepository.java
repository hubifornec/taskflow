package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.SimulacionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link com.ingenieria.taskflow.model.SimulacionItem}.
 * <p>
 * Gestiona los items del backlog de cada simulacion. Provee consultas para
 * obtener todos los items o solo los seleccionados para el sprint.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
public interface SimulacionItemRepository extends JpaRepository<SimulacionItem, Long> {

    /**
     * Obtiene todos los items del backlog de una simulacion especifica.
     *
     * @param simulacionId ID de la simulacion
     * @return lista de todos los items del backlog (en sprint y fuera de el)
     */
    List<SimulacionItem> findBySimulacionId(Long simulacionId);

    /**
     * Obtiene los items de una simulacion filtrados por si fueron incluidos en el sprint.
     * Usado en {@code cerrarSprint()} para calcular los puntos planeados y entregados.
     *
     * @param simulacionId ID de la simulacion
     * @param enSprint     {@code true} para obtener los del sprint, {@code false} para el backlog restante
     * @return lista de items que coinciden con el filtro {@code enSprint}
     */
    List<SimulacionItem> findBySimulacionIdAndEnSprint(Long simulacionId, Boolean enSprint);
}
