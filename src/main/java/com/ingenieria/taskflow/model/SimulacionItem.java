package com.ingenieria.taskflow.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa un item del backlog dentro de una {@link Simulacion}.
 * <p>
 * Al iniciar cada simulacion, el sistema genera automaticamente 12 items predefinidos
 * con historias de usuario reales de metodologia Scrum. El usuario puede seleccionar
 * cuales incluir en el sprint ({@code enSprint = true}) y luego marcarlos como
 * completados durante la ejecucion.
 * Mapeada a la tabla {@code simulacion_items} en PostgreSQL.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Data
@Entity
@Table(name = "simulacion_items")
public class SimulacionItem {

    /**
     * Identificador unico autogenerado por la base de datos (BIGSERIAL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador de la simulacion a la que pertenece este item (FK logica a {@code simulaciones.id}).
     */
    @Column(name = "simulacion_id")
    private Long simulacionId;

    /**
     * Nombre de la historia de usuario o tarea del backlog simulado.
     */
    private String titulo;

    /**
     * Estimacion del esfuerzo requerido para completar este item, en story points.
     */
    @Column(name = "story_points")
    private Integer storyPoints;

    /**
     * Indica si el item fue seleccionado para ser ejecutado en el sprint actual.
     * {@code false} por defecto; cambia a {@code true} mediante el endpoint toggle.
     */
    @Column(name = "en_sprint")
    private Boolean enSprint;

    /**
     * Indica si el item fue marcado como entregado durante la ejecucion del sprint.
     * {@code false} por defecto.
     */
    private Boolean completado;

    /**
     * Posicion de visualizacion del item dentro del backlog (orden ascendente).
     */
    private Integer orden;
}
