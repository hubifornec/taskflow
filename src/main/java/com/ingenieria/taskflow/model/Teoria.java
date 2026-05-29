package com.ingenieria.taskflow.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa un bloque de contenido teorico asociado a un {@link Cuestionario}.
 * <p>
 * El material teorico se presenta al estudiante antes de responder el quiz,
 * ordenado segun el campo {@code orden}. Puede contener texto plano o HTML.
 * Mapeada a la tabla {@code teorias} en PostgreSQL.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Data
@Entity
@Table(name = "teorias")
public class Teoria {

    /**
     * Identificador unico autogenerado por la base de datos (BIGSERIAL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador del cuestionario al que pertenece este bloque teorico
     * (FK logica a {@code cuestionarios.id}).
     */
    @Column(name = "cuestionario_id")
    private Long cuestionarioId;

    /**
     * Titulo del bloque teorico mostrado como encabezado al estudiante.
     */
    private String titulo;

    /**
     * Contenido del material teorico. Puede contener texto plano o HTML enriquecido.
     */
    @Column(columnDefinition = "TEXT")
    private String contenido;

    /**
     * Posicion de este bloque dentro del material teorico del cuestionario (orden ascendente).
     */
    private Integer orden;

    /**
     * Referencia bibliografica, URL o fuente de donde proviene el contenido teorico.
     */
    private String fuente;
}