package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.Simulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SimulacionRepository extends JpaRepository<Simulacion, Long> {
    Optional<Simulacion> findByUsuarioIdAndEstado(Long usuarioId, String estado);
    List<Simulacion> findByUsuarioId(Long usuarioId);
}
