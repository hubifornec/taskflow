package com.ingenieria.taskflow.service;

import com.ingenieria.taskflow.model.*;
import com.ingenieria.taskflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GamificacionService {

    @Autowired private LogroRepository logroRepository;
    @Autowired private LogroUsuarioRepository logroUsuarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TareaRepository tareaRepository;

    public Map<String, Object> procesarActividad(Long usuarioId, String tipoActividad) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Map<String, Object>> nuevosLogros = new ArrayList<>();
        int puntosGanados = 0;

        switch (tipoActividad) {
            case "TAREA_CREADA":
                puntosGanados = 10;
                long totalTareas = tareaRepository.countByUsuarioId(usuarioId);
                if (totalTareas == 1)
                    nuevosLogros.addAll(otorgarLogro(usuario, "PRIMERA_TAREA"));
                break;
            case "TAREA_COMPLETADA":
                puntosGanados = 20;
                long completadas = tareaRepository
                    .findByUsuarioIdAndEstado(usuarioId, "completada").size();
                if (completadas >= 5)
                    nuevosLogros.addAll(otorgarLogro(usuario, "CINCO_TAREAS"));
                if (completadas >= 10)
                    nuevosLogros.addAll(otorgarLogro(usuario, "DIEZ_TAREAS"));
                break;
            case "QUIZ_COMPLETADO":
                puntosGanados = 50;
                int quizzes = usuario.getQuizzesCompletados();
                if (quizzes == 1)
                    nuevosLogros.addAll(otorgarLogro(usuario, "PRIMER_QUIZ"));
                if (quizzes >= 4)
                    nuevosLogros.addAll(otorgarLogro(usuario, "TODOS_BASICO"));
                if (quizzes >= 8)
                    nuevosLogros.addAll(otorgarLogro(usuario, "TODOS_AVANZADO"));
                break;
            case "NIVEL_AVANZADO":
                nuevosLogros.addAll(otorgarLogro(usuario, "NIVEL_AVANZADO"));
                break;
        }

        usuario.setPuntos((usuario.getPuntos() == null ? 0 : usuario.getPuntos()) + puntosGanados);
        usuarioRepository.save(usuario);

        return Map.of(
            "puntosGanados", puntosGanados,
            "puntosTotal",   usuario.getPuntos(),
            "nuevosLogros",  nuevosLogros
        );
    }

    private List<Map<String, Object>> otorgarLogro(Usuario usuario, String codigo) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        Optional<Logro> logro = logroRepository.findByCodigo(codigo);
        if (logro.isEmpty()) return resultado;

        boolean yaExiste = logroUsuarioRepository
            .existsByUsuarioIdAndLogroId(usuario.getId(), logro.get().getId());
        if (yaExiste) return resultado;

        LogroUsuario lu = new LogroUsuario();
        lu.setUsuarioId(usuario.getId());
        lu.setLogro(logro.get());
        lu.setObtenidoAt(LocalDateTime.now());
        logroUsuarioRepository.save(lu);

        usuario.setPuntos((usuario.getPuntos() == null ? 0 : usuario.getPuntos())
            + logro.get().getPuntosRecompensa());

        resultado.add(Map.of(
            "codigo", logro.get().getCodigo(),
            "nombre", logro.get().getNombre(),
            "icono",  logro.get().getIcono(),
            "puntos", logro.get().getPuntosRecompensa()
        ));
        return resultado;
    }

    public List<LogroUsuario> obtenerLogrosUsuario(Long usuarioId) {
        return logroUsuarioRepository.findByUsuarioId(usuarioId);
    }

    public List<Logro> obtenerTodosLogros() {
        return logroRepository.findAll();
    }
}
