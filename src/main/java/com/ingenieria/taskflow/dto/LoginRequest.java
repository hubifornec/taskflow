package com.ingenieria.taskflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO que encapsula las credenciales de autenticacion del usuario.
 * <p>
 * Utilizado en el endpoint {@code POST /login}. Validado con {@code @Valid};
 * si las credenciales son incorrectas, {@code AuthService} lanza
 * {@code BadRequestException} con HTTP 400.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Data
public class LoginRequest {

    /**
     * Correo electronico del usuario. Obligatorio y debe tener formato valido.
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    /**
     * Contrasena en texto plano para verificar contra el hash BCrypt almacenado.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
