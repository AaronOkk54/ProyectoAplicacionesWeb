package Proyecto_DW.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * LoginController
 * Proporciona vistas de login y recuperación de contraseña.
 * Nota: La funcionalidad de autenticación ha sido deshabilitada.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "redirect:/auth/login";
    }

    @GetMapping("/recuperar")
    public String recuperar() {
        return "auth/recuperar";
    }
}