package com.ingenieria.taskflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true)
    private String email;

    private String password;

    private String nivel = "basico";

    @Column(name = "quizzes_completados")
    private Integer quizzesCompletados = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private Integer puntos = 0;
}