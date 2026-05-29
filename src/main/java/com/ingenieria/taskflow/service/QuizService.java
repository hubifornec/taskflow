package com.ingenieria.taskflow.service;

import com.ingenieria.taskflow.model.Cuestionario;
import com.ingenieria.taskflow.model.Pregunta;
import com.ingenieria.taskflow.model.Teoria;
import com.ingenieria.taskflow.repository.CuestionarioRepository;
import com.ingenieria.taskflow.repository.PreguntaRepository;
import com.ingenieria.taskflow.repository.TeoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * Servicio que gestiona el modulo educativo de quizzes y teoria de TaskFlow.
 * <p>
 * Provee cuestionarios, preguntas y material teorico, ademas de validar
 * las respuestas del estudiante de forma insensible a mayusculas y espacios.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Service
public class QuizService {

    @Autowired
    private CuestionarioRepository cuestionarioRepository;

    @Autowired
    private PreguntaRepository preguntaRepository;

    @Autowired
    private TeoriaRepository teoriaRepository;

    /**
     * Retorna todos los cuestionarios disponibles en el sistema.
     *
     * @return lista completa de cuestionarios
     */
    public List<Cuestionario> obtenerTodos() {
        return cuestionarioRepository.findAll();
    }

    /**
     * Retorna los cuestionarios filtrados por nivel de dificultad.
     *
     * @param nivel nivel a filtrar: {@code "basico"} o {@code "avanzado"}
     * @return lista de cuestionarios del nivel indicado
     */
    public List<Cuestionario> obtenerPorNivel(String nivel) {
        return cuestionarioRepository.findByNivel(nivel);
    }
    

    /**
     * Retorna todas las preguntas de un cuestionario especifico.
     *
     * @param cuestionarioId ID del cuestionario
     * @return lista de preguntas asociadas al cuestionario
     */
    public List<Pregunta> obtenerPreguntasPorCuestionario(Long cuestionarioId) {
        return preguntaRepository.findByCuestionarioId(cuestionarioId);
    }

    /**
     * Retorna el material teorico de un cuestionario ordenado para su presentacion.
     *
     * @param cuestionarioId ID del cuestionario
     * @return lista de bloques teoricos ordenados ascendentemente por {@code orden}
     */
    public List<Teoria> obtenerTeoriaPorCuestionario(Long cuestionarioId) {
        return teoriaRepository.findByCuestionarioIdOrderByOrden(cuestionarioId);
    }

    /**
     * Valida la respuesta del estudiante para una pregunta especifica.
     * La comparacion es insensible a mayusculas y elimina espacios en blanco con trim().
     *
     * @param preguntaId ID de la pregunta a validar
     * @param respuesta  respuesta proporcionada por el estudiante
     * @return mapa con: {@code correcto} (boolean), {@code respuestaCorrecta}, {@code explicacion}, {@code puntaje}
     * @throws RuntimeException si la pregunta no existe
     */
    public Map<String, Object> validarRespuesta(Long preguntaId, String respuesta) {
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> new RuntimeException("Pregunta no encontrada"));
        boolean correcto = pregunta.getRespuestaCorrecta().equalsIgnoreCase(respuesta.trim());
        String explicacion = pregunta.getExplicacion() != null ? pregunta.getExplicacion() : "";
        return Map.of(
                "correcto", correcto,
                "respuestaCorrecta", pregunta.getRespuestaCorrecta(),
                "explicacion", explicacion,
                "puntaje", correcto ? pregunta.getPuntaje() : 0
        );
    }
}