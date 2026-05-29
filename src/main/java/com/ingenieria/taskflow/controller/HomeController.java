package com.ingenieria.taskflow.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador MVC que redirige la raiz de la aplicacion a la pagina de login.
 * <p>
 * Al acceder a {@code /}, Spring reenvía la peticion a {@code /login.html}
 * que se sirve como recurso estatico desde {@code src/main/resources/static/}.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@Controller
public class HomeController {

    /**
     * Redirige la raiz de la aplicacion a la pagina de inicio de sesion.
     *
     * @return forward a {@code /login.html}
     */
    @GetMapping("/")
    public String home() {
        return "forward:/login.html";
    }
}