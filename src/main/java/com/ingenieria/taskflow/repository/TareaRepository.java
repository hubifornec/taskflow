package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;

public interface TareaRepository extends JpaRepository<Tarea, Long> {
    List<Tarea> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);
    List<Tarea> findByUsuarioId(Long usuarioId);
    List<Tarea> findByUsuarioIdAndEstado(Long usuarioId, String estado);
    long countByUsuarioId(Long usuarioId);
    long countByUsuarioIdAndEstado(Long usuarioId, String estado);

    @Query("SELECT t.estado AS estado, COUNT(t) AS total FROM Tarea t WHERE t.usuarioId = :usuarioId GROUP BY t.estado")
    List<Map<String, Object>> countGroupByEstado(@Param("usuarioId") Long usuarioId);
}
