package com.ingenieria.taskflow.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "simulacion_items")
public class SimulacionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "simulacion_id")
    private Long simulacionId;

    private String titulo;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Column(name = "en_sprint")
    private Boolean enSprint;

    private Boolean completado;
    private Integer orden;
}
