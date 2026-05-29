package com.ingenieria.taskflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO que encapsula los datos necesarios para registrar un nuevo usuario.
 * <p>
 * Validado automaticamente por Spring mediante {@code @Valid} en el controlador.
 * Si alguna restriccion no se cumple, se lanza {@code MethodArgumentNotValidException}
 * y el {@code GlobalExceptionHandler} responde con HTTP 400.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Data
public class RegisterRequest {

    /**
     * Nombre completo del usuario. Obligatorio, entre 2 y 100 caracteres.
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    /**
     * Correo electronico del usuario. Obligatorio y debe tener formato valido.
     * Se almacena en minusculas en el sistema.
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    /**
     * Contrasena en texto plano. Obligatoria, minimo 8 caracteres.
     * Adicionalmente validada por {@code AuthService.validarPassword()}:
     * debe contener mayuscula, numero y caracter especial.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    private String password;
}
