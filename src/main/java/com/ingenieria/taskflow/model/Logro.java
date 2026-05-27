package com.ingenieria.taskflow.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "logros")
public class Logro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private String nombre;
    private String descripcion;
    private String icono;

    @Column(name = "puntos_recompensa")
    private Integer puntosRecompensa;
}
