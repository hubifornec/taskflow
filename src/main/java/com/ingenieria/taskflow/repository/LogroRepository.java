package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.Logro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link com.ingenieria.taskflow.model.Logro}.
 * <p>
 * Utilizado por {@code GamificacionService} para buscar logros por su codigo
 * de negocio antes de otorgarlos a un usuario.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
public interface LogroRepository extends JpaRepository<Logro, Long> {

    /**
     * Busca un logro por su codigo de negocio unico.
     * Ejemplos de codigos: {@code "PRIMERA_TAREA"}, {@code "NIVEL_AVANZADO"}.
     *
     * @param codigo identificador de negocio del logro
     * @return {@code Optional} con el logro si existe, vacio si el codigo no existe
     */
    Optional<Logro> findByCodigo(String codigo);
}
