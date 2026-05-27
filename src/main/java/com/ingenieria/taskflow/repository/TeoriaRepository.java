package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.Teoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeoriaRepository extends JpaRepository<Teoria, Long> {
    List<Teoria> findByCuestionarioIdOrderByOrden(Long cuestionarioId);
}