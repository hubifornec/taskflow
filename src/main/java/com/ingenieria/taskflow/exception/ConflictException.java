package com.ingenieria.taskflow.exception;

/**
 * Excepcion que representa un conflicto de datos (HTTP 409 Conflict).
 * <p>
 * Se lanza cuando una operacion viola una restriccion de unicidad, por ejemplo
 * al intentar registrar un email que ya existe en el sistema.
 * Capturada y manejada por {@link GlobalExceptionHandler}.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
public class ConflictException extends RuntimeException {

    /**
     * Construye la excepcion con un mensaje descriptivo del conflicto.
     *
     * @param message descripcion del conflicto detectado
     */
    public ConflictException(String message) {
        super(message);
    }
}
