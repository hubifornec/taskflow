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

@Service
public class QuizService {

    @Autowired
    private CuestionarioRepository cuestionarioRepository;

    @Autowired
    private PreguntaRepository preguntaRepository;

    @Autowired
    private TeoriaRepository teoriaRepository;

    public List<Cuestionario> obtenerTodos() {
        return cuestionarioRepository.findAll();
    }

    public List<Cuestionario> obtenerPorNivel(String nivel) {
        return cuestionarioRepository.findByNivel(nivel);
    }
    

    public List<Pregunta> obtenerPreguntasPorCuestionario(Long cuestionarioId) {
        return preguntaRepository.findByCuestionarioId(cuestionarioId);
    }

    public List<Teoria> obtenerTeoriaPorCuestionario(Long cuestionarioId) {
        return teoriaRepository.findByCuestionarioIdOrderByOrden(cuestionarioId);
    }

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