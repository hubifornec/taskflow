package com.ingenieria.taskflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tareas")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    private String descripcion;

    @Column(name = "columna_id")
    private Integer columnaId;

    private Integer prioridad = 5;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    private String estado = "pendiente";

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "puntos_historia")
    private Integer puntosHistoria = 3;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
