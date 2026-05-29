package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.Teoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link com.ingenieria.taskflow.model.Teoria}.
 * <p>
 * Permite obtener el material teorico de un cuestionario ordenado para
 * presentarlo al estudiante en el orden correcto antes del quiz.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
public interface TeoriaRepository extends JpaRepository<Teoria, Long> {

    /**
     * Obtiene los bloques teoricos de un cuestionario ordenados ascendentemente por su campo {@code orden}.
     *
     * @param cuestionarioId ID del cuestionario al que pertenece la teoria
     * @return lista de bloques teoricos ordenados para presentacion secuencial
     */
    List<Teoria> findByCuestionarioIdOrderByOrden(Long cuestionarioId);
}
