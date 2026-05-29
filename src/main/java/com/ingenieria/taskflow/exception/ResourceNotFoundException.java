package com.ingenieria.taskflow.exception;

/**
 * Excepcion que representa un recurso no encontrado (HTTP 404 Not Found).
 * <p>
 * Se lanza cuando se intenta acceder o modificar una entidad que no existe
 * en la base de datos, por ejemplo una tarea, usuario o cuestionario con
 * un ID inexistente.
 * Capturada y manejada por {@link GlobalExceptionHandler}.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Construye la excepcion con un mensaje descriptivo del recurso no encontrado.
     *
     * @param message descripcion del recurso que no fue encontrado
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
