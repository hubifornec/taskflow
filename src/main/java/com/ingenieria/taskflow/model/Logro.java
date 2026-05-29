package com.ingenieria.taskflow.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que define un logro del sistema de gamificacion de TaskFlow.
 * <p>
 * Los logros son globales y se definen una sola vez en la base de datos.
 * La obtencion de un logro por parte de un usuario especifico se registra
 * en la entidad {@link LogroUsuario}.
 * Codigos de logro disponibles: PRIMERA_TAREA, CINCO_TAREAS, DIEZ_TAREAS,
 * PRIMER_QUIZ, TODOS_BASICO, TODOS_AVANZADO, NIVEL_AVANZADO.
 * Mapeada a la tabla {@code logros} en PostgreSQL.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Data
@Entity
@Table(name = "logros")
public class Logro {

    /**
     * Identificador unico autogenerado por la base de datos (BIGSERIAL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador unico de negocio del logro.
     * Ejemplos: {@code "PRIMERA_TAREA"}, {@code "NIVEL_AVANZADO"}.
     * Usado internamente por {@code GamificacionService} para buscar logros por codigo.
     */
    private String codigo;

    /**
     * Nombre legible del logro mostrado al usuario en la interfaz.
     */
    private String nombre;

    /**
     * Descripcion del criterio necesario para obtener el logro.
     */
    private String descripcion;

    /**
     * Emoji o codigo de icono representativo que se muestra junto al logro en la UI.
     */
    private String icono;

    /**
     * Puntos adicionales que se suman al total del usuario al obtener este logro.
     */
    @Column(name = "puntos_recompensa")
    private Integer puntosRecompensa;
}
