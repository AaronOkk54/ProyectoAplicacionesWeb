/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Proyecto_DW.controller;

import Proyecto_DW.domain.Usuario;
import Proyecto_DW.service.RegistroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Alfaro
 */
@Controller
@RequestMapping("/registro")
public class RegistroController {

    private final RegistroService registroService;

    public RegistroController(RegistroService registroService) {
        this.registroService = registroService;
    }

    /**
     * Muestra el formulario de registro.
     * Ruta: GET /registro/nuevo
     */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/registro";
    }

    /**
     * Procesa el formulario de registro.
     * Ruta: POST /registro/crearUsuario
     */
    @PostMapping("/crearUsuario")
    public String crearUsuario(@ModelAttribute("usuario") Usuario usuario,
                               @RequestParam(defaultValue = "CLIENTE") String rol,
                               Model model) {
        try {
            registroService.crearUsuario(model, usuario);
            return "redirect:/auth/login?registrado=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("errores", java.util.List.of(e.getMessage()));
            return "auth/registro";
        } catch (Exception e) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("errores", java.util.List.of("Ocurrió un error inesperado. Intentá de nuevo."));
            return "auth/registro";
        }
    }

    /**
     * Muestra el formulario de recuperación de contraseña.
     * Ruta: GET /registro/recordar
     */
    @GetMapping("/recordar")
    public String recordar() {
        return "auth/recuperar";
    }

    /**
     * Procesa la solicitud de recuperación de contraseña.
     * Ruta: POST /registro/recordarUsuario
     */
    /*@PostMapping("/recordarUsuario")
    public String recordarUsuario(@RequestParam("email") String email, Model model) {
        try {
            registroService.recordarUsuario(email);
            model.addAttribute("success",
                "Te enviamos un correo con las instrucciones. Revisá tu bandeja de entrada.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "auth/recuperar";
    }*/
}
