package com.ingenieria.taskflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO que encapsula los datos para crear o editar una tarea en el tablero Kanban.
 * <p>
 * Validado automaticamente por Spring mediante {@code @Valid}. Utilizado en los
 * endpoints {@code POST /tareas} y {@code PUT /tareas/{id}}.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Data
public class TareaRequest {

    /**
     * Titulo de la tarea. Obligatorio, maximo 255 caracteres.
     */
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede superar 255 caracteres")
    private String titulo;

    /**
     * Descripcion detallada opcional. Maximo 1000 caracteres.
     */
    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    private String descripcion;

    /**
     * ID del usuario propietario de la tarea. Obligatorio.
     */
    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;

    /**
     * Nivel de prioridad entre 1 (alta) y 10 (baja). Opcional; defecto 5.
     */
    @Min(value = 1, message = "La prioridad mínima es 1")
    @Max(value = 10, message = "La prioridad máxima es 10")
    private Integer prioridad;

    /**
     * Fecha limite de entrega de la tarea. Opcional.
     */
    private LocalDate fechaVencimiento;

    /**
     * Estimacion de esfuerzo en story points. Opcional; defecto 3.
     */
    private Integer puntosHistoria;
}
