package com.ingenieria.taskflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad que representa una sesion de sprint planning simulado en TaskFlow.
 * <p>
 * Cada usuario puede tener como maximo una simulacion en estado {@code "activo"} a la vez.
 * Al iniciar una nueva, la simulacion activa previa cambia automaticamente a {@code "abandonado"}.
 * Una vez ejecutado el sprint y evaluados los resultados, pasa a estado {@code "cerrado"}.
 * Mapeada a la tabla {@code simulaciones} en PostgreSQL.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Data
@Entity
@Table(name = "simulaciones")
public class Simulacion {

    /**
     * Identificador unico autogenerado por la base de datos (BIGSERIAL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador del usuario que ejecuta la simulacion (FK logica a {@code usuarios.id}).
     */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /**
     * Estado actual de la simulacion.
     * Valores posibles: {@code "activo"}, {@code "cerrado"}, {@code "abandonado"}.
     */
    private String estado;

    /**
     * Objetivo del sprint definido por el usuario al iniciar la simulacion.
     * Serializado en JSON como {@code "metaSprint"} mediante {@code @JsonProperty}.
     */
    @JsonProperty("metaSprint")
    @Column(name = "sprint_goal")
    private String sprintGoal;

    /**
     * Velocidad del equipo expresada en story points por sprint.
     * Sirve como referencia para evaluar si el sprint fue exitoso.
     */
    @Column(name = "velocidad_equipo")
    private Integer velocidadEquipo;

    /**
     * Fecha y hora en que se inicio la simulacion.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Fecha y hora en que se cerro el sprint. Es {@code null} mientras la simulacion este activa.
     */
    @Column(name = "cerrado_at")
    private LocalDateTime cerradoAt;
}
