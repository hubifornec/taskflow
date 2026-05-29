package com.ingenieria.taskflow.service;

import com.ingenieria.taskflow.model.*;
import com.ingenieria.taskflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio que implementa el sistema de gamificacion de TaskFlow.
 * <p>
 * Procesa actividades del usuario (crear tareas, completarlas, aprobar quizzes,
 * subir de nivel) y otorga puntos y logros segun los umbrales definidos.
 * Los tipos de actividad soportados son: {@code TAREA_CREADA} (+10 pts),
 * {@code TAREA_COMPLETADA} (+20 pts), {@code QUIZ_COMPLETADO} (+50 pts),
 * {@code NIVEL_AVANZADO} (otorga logro, sin puntos adicionales directos).
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Service
public class GamificacionService {

    @Autowired private LogroRepository logroRepository;
    @Autowired private LogroUsuarioRepository logroUsuarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TareaRepository tareaRepository;

    /**
     * Procesa una actividad del usuario y otorga puntos y logros correspondientes.
     * Tipos soportados: {@code TAREA_CREADA} (+10), {@code TAREA_COMPLETADA} (+20),
     * {@code QUIZ_COMPLETADO} (+50), {@code NIVEL_AVANZADO} (logro sin puntos directos).
     *
     * @param usuarioId     ID del usuario que realizo la actividad
     * @param tipoActividad tipo de actividad realizada
     * @return mapa con: {@code puntosGanados}, {@code puntosTotal}, {@code nuevosLogros}
     * @throws RuntimeException si el usuario no existe
     */
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

    /**
     * Intenta otorgar un logro al usuario si aun no lo tiene.
     * Si el logro no existe en la base de datos o el usuario ya lo tiene, no hace nada.
     *
     * @param usuario entidad del usuario que podria recibir el logro
     * @param codigo  codigo del logro a otorgar (ej: {@code "PRIMERA_TAREA"})
     * @return lista con los datos del logro otorgado, o lista vacia si no se otorgo
     */
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

    /**
     * Obtiene todos los logros obtenidos por un usuario especifico.
     *
     * @param usuarioId ID del usuario
     * @return lista de registros {@code LogroUsuario} con el logro y la fecha de obtencion
     */
    public List<LogroUsuario> obtenerLogrosUsuario(Long usuarioId) {
        return logroUsuarioRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Obtiene la definicion completa de todos los logros disponibles en el sistema.
     *
     * @return lista de todos los logros registrados en la base de datos
     */
    public List<Logro> obtenerTodosLogros() {
        return logroRepository.findAll();
    }
}
