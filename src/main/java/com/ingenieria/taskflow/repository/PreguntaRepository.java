package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link com.ingenieria.taskflow.model.Pregunta}.
 * <p>
 * Permite obtener todas las preguntas de un cuestionario dado, utilizado
 * por {@code QuizService} al cargar un quiz para el estudiante.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    /**
     * Obtiene todas las preguntas pertenecientes a un cuestionario especifico.
     *
     * @param cuestionarioId ID del cuestionario padre
     * @return lista de preguntas del cuestionario, puede estar vacia
     */
    List<Pregunta> findByCuestionarioId(Long cuestionarioId);
}
