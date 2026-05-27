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

    private String titulo;
    private String descripcion;

    @Column(name = "columna_id")
    private Integer columnaId;

    private Integer prioridad;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    private String estado = "pendiente";

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}