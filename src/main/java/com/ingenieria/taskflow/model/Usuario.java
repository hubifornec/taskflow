package com.ingenieria.taskflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad que representa a un usuario registrado en el sistema TaskFlow.
 * <p>
 * Almacena las credenciales de autenticacion, el nivel de avance educativo
 * (basico/avanzado) y los puntos acumulados por el sistema de gamificacion.
 * Mapeada a la tabla {@code usuarios} en PostgreSQL.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    /**
     * Identificador unico autogenerado por la base de datos (BIGSERIAL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre completo del usuario tal como fue ingresado en el registro.
     */
    private String nombre;

    /**
     * Correo electronico del usuario. Debe ser unico en el sistema.
     * Se almacena en minusculas para garantizar unicidad sin distincion de mayusculas.
     */
    @Column(unique = true)
    private String email;

    /**
     * Contrasena del usuario cifrada con BCrypt.
     * Nunca se almacena en texto plano.
     */
    private String password;

    /**
     * Nivel de acceso educativo del usuario.
     * Valores posibles: {@code "basico"} (por defecto) o {@code "avanzado"}.
     * Cambia automaticamente cuando el usuario completa 4 o mas quizzes.
     */
    private String nivel = "basico";

    /**
     * Cantidad de quizzes completados por el usuario.
     * Cuando alcanza 4, el nivel asciende automaticamente a "avanzado".
     */
    @Column(name = "quizzes_completados")
    private Integer quizzesCompletados = 0;

    /**
     * Fecha y hora en que el usuario se registro en el sistema.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Puntos acumulados por el usuario a traves del sistema de gamificacion.
     * Se suman puntos al crear tareas, completarlas y aprobar quizzes.
     */
    private Integer puntos = 0;
}