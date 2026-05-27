package com.ingenieria.taskflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "simulaciones")
public class Simulacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    private String estado;

    @Column(name = "sprint_goal")
    private String sprintGoal;

    @Column(name = "velocidad_equipo")
    private Integer velocidadEquipo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "cerrado_at")
    private LocalDateTime cerradoAt;
}
