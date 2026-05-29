package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.Simulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link com.ingenieria.taskflow.model.Simulacion}.
 * <p>
 * Utilizado por {@code SimulacionService} para gestionar el ciclo de vida
 * de las simulaciones de sprint. La consulta mas critica es la de simulacion
 * activa por usuario, que garantiza que solo exista una a la vez.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
public interface SimulacionRepository extends JpaRepository<Simulacion, Long> {

    /**
     * Busca la simulacion de un usuario en un estado especifico.
     * Tipicamente usado para encontrar la simulacion activa ({@code estado = "activo"}).
     *
     * @param usuarioId ID del usuario
     * @param estado    estado buscado: {@code "activo"}, {@code "cerrado"} o {@code "abandonado"}
     * @return {@code Optional} con la simulacion si existe en ese estado, vacio si no
     */
    Optional<Simulacion> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    /**
     * Obtiene el historial completo de simulaciones de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return lista de todas las simulaciones del usuario en cualquier estado
     */
    List<Simulacion> findByUsuarioId(Long usuarioId);
}
