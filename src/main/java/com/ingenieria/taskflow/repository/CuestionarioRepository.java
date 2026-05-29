package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.Cuestionario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link com.ingenieria.taskflow.model.Cuestionario}.
 * <p>
 * Provee metodos para filtrar cuestionarios por nivel de dificultad y tipo.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
public interface CuestionarioRepository extends JpaRepository<Cuestionario, Long> {

    /**
     * Obtiene todos los cuestionarios de un nivel de dificultad especifico.
     *
     * @param nivel nivel de dificultad: {@code "basico"} o {@code "avanzado"}
     * @return lista de cuestionarios del nivel indicado
     */
    List<Cuestionario> findByNivel(String nivel);

    /**
     * Obtiene cuestionarios filtrados por nivel y tipo simultaneamente.
     *
     * @param nivel nivel de dificultad: {@code "basico"} o {@code "avanzado"}
     * @param tipo  tipo de evaluacion: {@code "quiz"} o {@code "evaluacion"}
     * @return lista de cuestionarios que coinciden con ambos filtros
     */
    List<Cuestionario> findByNivelAndTipo(String nivel, String tipo);
}
