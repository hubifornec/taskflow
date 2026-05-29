package com.ingenieria.taskflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad de union que registra los logros obtenidos por cada usuario.
 * <p>
 * Actua como tabla intermedia entre {@link Usuario} y {@link Logro}.
 * Cada registro representa la obtencion de un logro especifico por un usuario
 * en un momento determinado. Es la unica entidad con una relacion
 * {@code @ManyToOne} JPA explicita hacia {@link Logro}.
 * Mapeada a la tabla {@code logros_usuario} en PostgreSQL.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Data
@Entity
@Table(name = "logros_usuario")
public class LogroUsuario {

    /**
     * Identificador unico autogenerado por la base de datos (BIGSERIAL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador del usuario que obtuvo el logro (FK logica a {@code usuarios.id}).
     */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /**
     * Logro obtenido por el usuario. Relacion ManyToOne hacia la tabla {@code logros}.
     */
    @ManyToOne
    @JoinColumn(name = "logro_id")
    private Logro logro;

    /**
     * Fecha y hora exacta en que el usuario obtuvo el logro.
     */
    @Column(name = "obtenido_at")
    private LocalDateTime obtenidoAt;
}
