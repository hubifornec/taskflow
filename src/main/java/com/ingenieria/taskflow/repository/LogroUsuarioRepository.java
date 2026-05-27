package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.LogroUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogroUsuarioRepository extends JpaRepository<LogroUsuario, Long> {
    List<LogroUsuario> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioIdAndLogroId(Long usuarioId, Long logroId);
}
