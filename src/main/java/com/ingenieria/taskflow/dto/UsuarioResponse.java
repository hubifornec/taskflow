package com.ingenieria.taskflow.dto;

import com.ingenieria.taskflow.model.Usuario;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String email;
    private String nivel;
    private Integer quizzesCompletados;
    private Integer puntos;
    private LocalDateTime createdAt;

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
