package com.ingenieria.taskflow.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para todos los controladores REST de TaskFlow.
 * <p>
 * Intercepta las excepciones lanzadas en cualquier capa del sistema y las convierte
 * en respuestas HTTP estructuradas con timestamp, codigo de estado y mensaje de error.
 * Utiliza {@code @RestControllerAdvice} de Spring para aplicarse globalmente.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja recursos no encontrados. Retorna HTTP 404.
     *
     * @param ex excepcion lanzada cuando una entidad no existe en la base de datos
     * @return respuesta con status 404 y mensaje de error
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Maneja solicitudes invalidas. Retorna HTTP 400.
     *
     * @param ex excepcion lanzada por credenciales incorrectas, estados invalidos, etc.
     * @return respuesta con status 400 y mensaje de error
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Maneja conflictos de datos. Retorna HTTP 409.
     *
     * @param ex excepcion lanzada cuando ya existe un recurso con los mismos datos unicos
     * @return respuesta con status 409 y mensaje de error
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Maneja errores de validacion de Bean Validation ({@code @Valid}). Retorna HTTP 400.
     * La respuesta incluye el mapa {@code campos} con el nombre de cada campo invalido
     * y su mensaje de error correspondiente.
     *
     * @param ex excepcion lanzada cuando un DTO no pasa las validaciones de Jakarta
     * @return respuesta con status 400 y mapa de campos con errores
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            errores.put(campo, error.getDefaultMessage());
        });
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 400);
        body.put("error", "Datos inválidos");
        body.put("campos", errores);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Maneja cualquier excepcion no contemplada. Retorna HTTP 500.
     * Registra el error completo en el log del servidor.
     *
     * @param ex excepcion inesperada capturada como ultimo recurso
     * @return respuesta generica con status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    /**
     * Construye el cuerpo estandar de respuesta de error con timestamp, status y mensaje.
     *
     * @param status codigo HTTP del error
     * @param mensaje descripcion del error a incluir en la respuesta
     * @return {@code ResponseEntity} con el cuerpo de error serializado como JSON
     */
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String mensaje) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
