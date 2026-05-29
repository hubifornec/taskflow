package com.ingenieria.taskflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa una tarea dentro del tablero Kanban de TaskFlow.
 * <p>
 * Cada tarea pertenece a un usuario y puede encontrarse en uno de tres estados:
 * {@code pendiente}, {@code en_proceso} o {@code completada}. Al completarse,
 * el sistema de gamificacion otorga puntos al usuario propietario.
 * Mapeada a la tabla {@code tareas} en PostgreSQL.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Data
@Entity
@Table(name = "tareas")
public class Tarea {

    /**
     * Identificador unico autogenerado por la base de datos (BIGSERIAL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Titulo descriptivo de la tarea. Campo obligatorio (NOT NULL).
     */
    @Column(nullable = false)
    private String titulo;

    /**
     * Descripcion detallada opcional con informacion adicional sobre la tarea.
     */
    private String descripcion;

    /**
     * Columna del tablero Kanban donde se ubica la tarea.
     * Valores: 0=Backlog, 1=To Do, 2=In Progress, 3=Done.
     */
    @Column(name = "columna_id")
    private Integer columnaId;

    /**
     * Nivel de prioridad de la tarea en escala del 1 (alta) al 10 (baja).
     * Valor por defecto: 5.
     */
    private Integer prioridad = 5;

    /**
     * Fecha limite de entrega de la tarea. Opcional.
     */
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    /**
     * Estado actual de la tarea.
     * Valores validos: {@code "pendiente"} (defecto), {@code "en_proceso"}, {@code "completada"}.
     */
    private String estado = "pendiente";

    /**
     * Identificador del usuario propietario de la tarea (FK logica a {@code usuarios.id}).
     */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /**
     * Estimacion del esfuerzo requerido expresada en story points.
     * Valor por defecto: 3.
     */
    @Column(name = "puntos_historia")
    private Integer puntosHistoria = 3;

    /**
     * Fecha y hora en que fue creada la tarea.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
