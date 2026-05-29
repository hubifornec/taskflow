package com.ingenieria.taskflow.exception;

/**
 * Excepcion que representa un error de solicitud invalida (HTTP 400 Bad Request).
 * <p>
 * Se lanza cuando los datos enviados por el cliente son semanticamente incorrectos,
 * por ejemplo: credenciales incorrectas, estado de tarea invalido o contrasena
 * que no cumple los requisitos de seguridad.
 * Capturada y manejada por {@link GlobalExceptionHandler}.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
public class BadRequestException extends RuntimeException {

    /**
     * Construye la excepcion con un mensaje descriptivo del error.
     *
     * @param message descripcion del motivo del error de solicitud
     */
    public BadRequestException(String message) {
        super(message);
    }
}
