package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.Logro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LogroRepository extends JpaRepository<Logro, Long> {
    Optional<Logro> findByCodigo(String codigo);
}
