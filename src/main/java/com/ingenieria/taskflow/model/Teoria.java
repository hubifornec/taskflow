package com.ingenieria.taskflow.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "teorias")
public class Teoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuestionario_id")
    private Long cuestionarioId;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    private Integer orden;

    private String fuente;
}