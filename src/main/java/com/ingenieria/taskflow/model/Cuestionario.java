package com.ingenieria.taskflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa un cuestionario de evaluacion dentro del modulo educativo de TaskFlow.
 * <p>
 * Un cuestionario agrupa un conjunto de {@link Pregunta}s y puede ser de tipo
 * {@code "quiz"} o {@code "evaluacion"}. Tiene asociado un nivel de dificultad
 * ({@code "basico"} o {@code "avanzado"}) y material teorico previo ({@link Teoria}).
 * Mapeada a la tabla {@code cuestionarios} en PostgreSQL.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Data
@Entity
@Table(name = "cuestionarios")
public class Cuestionario {

    /**
     * Identificador unico autogenerado por la base de datos (BIGSERIAL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Titulo del cuestionario. Campo obligatorio, maximo 200 caracteres.
     */
    @Column(nullable = false, length = 200)
    private String titulo;

    /**
     * Descripcion general o instrucciones para el estudiante antes de iniciar.
     */
    private String descripcion;

    /**
     * Tiempo maximo permitido para completar el cuestionario, expresado en minutos.
     */
    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    /**
     * Tipo de evaluacion. Valores posibles: {@code "quiz"} o {@code "evaluacion"}.
     * Campo obligatorio.
     */
    @Column(nullable = false, length = 20)
    private String tipo;

    /**
     * Nivel de dificultad del cuestionario. Valores: {@code "basico"} o {@code "avanzado"}.
     */
    private String nivel;

    /**
     * Fecha y hora en que fue creado el cuestionario.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Lista de preguntas asociadas a este cuestionario.
     * Relacion bidireccional OneToMany. Excluida de la serializacion JSON
     * para evitar referencias circulares.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "cuestionario", cascade = CascadeType.ALL)
    private List<Pregunta> preguntas;
}