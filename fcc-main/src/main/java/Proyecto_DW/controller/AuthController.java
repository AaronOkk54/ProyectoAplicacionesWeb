package Proyecto_DW.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * AuthController
 * Controlador de autenticación que maneja las rutas /auth/*
 * Nota: Solo proporciona vistas. La funcionalidad de autenticación ha sido deshabilitada.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    /**
     * Página de login
     */
    @GetMapping("/login")
    public String login() {
        return "auth/inicioSesion";
    }

    /**
     * Formulario de registro
     */
    @GetMapping("/registro")
    public String registro() {
        return "auth/registro";
    }
}
