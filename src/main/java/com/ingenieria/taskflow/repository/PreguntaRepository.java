package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
    List<Pregunta> findByCuestionarioId(Long cuestionarioId);
}