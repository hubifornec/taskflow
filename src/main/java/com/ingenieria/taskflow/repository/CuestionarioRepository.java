package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.Cuestionario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CuestionarioRepository extends JpaRepository<Cuestionario, Long> {
    List<Cuestionario> findByNivel(String nivel);
    List<Cuestionario> findByNivelAndTipo(String nivel, String tipo);
}