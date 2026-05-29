package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.LogroUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link com.ingenieria.taskflow.model.LogroUsuario}.
 * <p>
 * Gestiona los registros de logros obtenidos por cada usuario.
 * Permite consultar los logros de un usuario y verificar si ya obtuvo un logro especifico.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
public interface LogroUsuarioRepository extends JpaRepository<LogroUsuario, Long> {

    /**
     * Obtiene todos los logros obtenidos por un usuario especifico.
     *
     * @param usuarioId ID del usuario
     * @return lista de registros de logros del usuario, puede estar vacia
     */
    List<LogroUsuario> findByUsuarioId(Long usuarioId);

    /**
     * Verifica si un usuario ya obtuvo un logro especifico.
     * Usado para evitar otorgar el mismo logro mas de una vez.
     *
     * @param usuarioId ID del usuario
     * @param logroId   ID del logro a verificar
     * @return {@code true} si el usuario ya tiene ese logro registrado
     */
    boolean existsByUsuarioIdAndLogroId(Long usuarioId, Long logroId);
}
