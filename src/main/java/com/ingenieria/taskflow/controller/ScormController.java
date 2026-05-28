package com.ingenieria.taskflow.controller;

import com.ingenieria.taskflow.service.ScormService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Endpoints de exportación SCORM 1.2.
 *
 * GET /api/scorm/app
 *   → Descarga la aplicación TaskFlow completa empaquetada como SCORM 1.2.
 *     Incluye login.html + index.html + todos los CSS/JS + scorm_init.js.
 *     Sube el ZIP a cualquier LMS (Moodle, Canvas, Blackboard, ILIAS…).
 *
 * GET /api/scorm/quiz/{cuestionarioId}
 *   → Descarga un cuestionario individual como paquete SCORM 1.2 autocontenido
 *     (HTML + teoría + quiz con tracking, sin depender de la API).
 */
@RestController
@RequestMapping("/api/scorm")
public class ScormController {

    private static final Logger log = LoggerFactory.getLogger(ScormController.class);

    @Autowired
    private ScormService scormService;

    /** Exporta TODA la aplicación TaskFlow como paquete SCORM 1.2 */
    @GetMapping("/app")
    public ResponseEntity<byte[]> exportarAplicacion() {
        try {
            byte[] zip = scormService.generarPaqueteAplicacion();
            return zipResponse(zip, "taskflow_scorm_app.zip");
        } catch (IOException e) {
            log.error("Error generando paquete SCORM de la aplicación: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Exporta un cuestionario individual como paquete SCORM 1.2 */
    @GetMapping("/quiz/{cuestionarioId}")
    public ResponseEntity<byte[]> exportarQuiz(@PathVariable Long cuestionarioId) {
        try {
            byte[] zip = scormService.generarPaqueteCuestionario(cuestionarioId);
            return zipResponse(zip, "taskflow_scorm_quiz_" + cuestionarioId + ".zip");
        } catch (IOException e) {
            log.error("Error generando paquete SCORM para cuestionario {}: {}", cuestionarioId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<byte[]> zipResponse(byte[] zip, String filename) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        h.setContentDispositionFormData("attachment", filename);
        h.setContentLength(zip.length);
        log.info("SCORM exportado: {} ({} bytes)", filename, zip.length);
        return ResponseEntity.ok().headers(h).body(zip);
    }
}
