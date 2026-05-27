package com.ingenieria.taskflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TareaRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede superar 255 caracteres")
    private String titulo;

    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    private String descripcion;

    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;

    @Min(value = 1, message = "La prioridad mínima es 1")
    @Max(value = 10, message = "La prioridad máxima es 10")
    private Integer prioridad;

    private LocalDate fechaVencimiento;

    private Integer puntosHistoria;
}
