package com.ingenieria.taskflow.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "preguntas")
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(columnDefinition = "TEXT")
    private String opciones;

    @Column(name = "respuesta_correcta", nullable = false, length = 500)
    private String respuestaCorrecta;

    @Column(name = "tipo_pregunta", length = 20)
    private String tipoPregunta;

    private Integer puntaje;

    @Column(columnDefinition = "TEXT")
    private String explicacion;

    @JsonIgnore  // ← esto rompe la referencia circular
    @ManyToOne
    @JoinColumn(name = "cuestionario_id", nullable = false)
    private Cuestionario cuestionario;
}