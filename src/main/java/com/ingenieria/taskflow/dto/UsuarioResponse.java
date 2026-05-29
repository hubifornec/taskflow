package com.ingenieria.taskflow.dto;

import com.ingenieria.taskflow.model.Usuario;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO de respuesta que expone los datos publicos de un usuario.
 * <p>
 * Excluye deliberadamente el campo {@code password} para evitar su exposicion
 * en las respuestas HTTP. Retornado por los endpoints de registro, login
 * y completar quiz.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Data
public class UsuarioResponse {

    /** Identificador unico del usuario. */
    private Long id;
    /** Nombre completo del usuario. */
    private String nombre;
    /** Correo electronico del usuario. */
    private String email;
    /** Nivel educativo actual: {@code "basico"} o {@code "avanzado"}. */
    private String nivel;
    /** Cantidad de quizzes completados hasta el momento. */
    private Integer quizzesCompletados;
    /** Puntos acumulados por gamificacion. */
    private Integer puntos;
    /** Fecha y hora de registro del usuario. */
    private LocalDateTime createdAt;

    /**
     * Metodo de fabrica que construye un {@code UsuarioResponse} a partir de la entidad {@link Usuario}.
     *
     * @param u entidad Usuario obtenida de la base de datos
     * @return DTO listo para serializar como respuesta JSON
     */
    public static UsuarioResponse from(Usuario u) {
        UsuarioResponse r = new UsuarioResponse();
        r.setId(u.getId());
        r.setNombre(u.getNombre());
        r.setEmail(u.getEmail());
        r.setNivel(u.getNivel());
        r.setQuizzesCompletados(u.getQuizzesCompletados());
        r.setPuntos(u.getPuntos());
        r.setCreatedAt(u.getCreatedAt());
        return r;
    }
}
