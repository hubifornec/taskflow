package com.ingenieria.taskflow.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa una pregunta individual dentro de un {@link Cuestionario}.
 * <p>
 * Soporta dos tipos de pregunta: opcion multiple ({@code "multiple"}) y respuesta
 * abierta ({@code "abierta"}). La validacion de respuestas se realiza de forma
 * insensible a mayusculas y con eliminacion de espacios en blanco (trim).
 * Mapeada a la tabla {@code preguntas} en PostgreSQL.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Data
@Entity
@Table(name = "preguntas")
public class Pregunta {

    /**
     * Identificador unico autogenerado por la base de datos (BIGSERIAL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Enunciado completo de la pregunta. Campo obligatorio almacenado como TEXT.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    /**
     * Opciones de respuesta para preguntas de tipo multiple.
     * Las opciones se separan con el caracter {@code |}.
     */
    @Column(columnDefinition = "TEXT")
    private String opciones;

    /**
     * Respuesta correcta de la pregunta. Campo obligatorio.
     * La comparacion se realiza con {@code equalsIgnoreCase(respuesta.trim())}.
     * Excluida de la serializacion JSON para evitar que el cliente conozca
     * la respuesta antes de contestar (seguridad del quiz).
     */
    @JsonIgnore
    @Column(name = "respuesta_correcta", nullable = false, length = 500)
    private String respuestaCorrecta;

    /**
     * Tipo de pregunta. Valores posibles: {@code "multiple"} o {@code "abierta"}.
     */
    @Column(name = "tipo_pregunta", length = 20)
    private String tipoPregunta;

    /**
     * Puntos que se otorgan al usuario al responder correctamente esta pregunta.
     */
    private Integer puntaje;

    /**
     * Texto de retroalimentacion mostrado al usuario tras responder,
     * independientemente de si la respuesta fue correcta o no.
     */
    @Column(columnDefinition = "TEXT")
    private String explicacion;

    /**
     * Cuestionario al que pertenece esta pregunta.
     * Relacion ManyToOne; excluida del JSON para evitar referencias circulares.
     */
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "cuestionario_id", nullable = false)
    private Cuestionario cuestionario;
}