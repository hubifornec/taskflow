package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link com.ingenieria.taskflow.model.Usuario}.
 * <p>
 * Extiende {@code JpaRepository} para heredar operaciones CRUD estandar.
 * Provee metodos de busqueda por email utilizados en autenticacion y registro.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo electronico.
     *
     * @param email correo electronico a buscar (debe estar en minusculas)
     * @return {@code Optional} con el usuario si existe, vacio si no
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si ya existe un usuario registrado con el email dado.
     * Usado en el registro para detectar emails duplicados.
     *
     * @param email correo electronico a verificar
     * @return {@code true} si ya existe un usuario con ese email
     */
    boolean existsByEmail(String email);
}
