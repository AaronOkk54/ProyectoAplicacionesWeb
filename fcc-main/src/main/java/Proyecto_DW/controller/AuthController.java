package Proyecto_DW.controller;

import Proyecto_DW.domain.Usuario;
import Proyecto_DW.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/inicioSesion";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario()); // <-- esto faltaba
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@ModelAttribute("usuario") Usuario usuario,
                            @RequestParam(defaultValue = "CLIENTE") String rol,
                            Model model) {
        try {
            usuarioService.registrar(usuario, rol);
            return "redirect:/auth/login?registrado=true";
        } catch (Exception e) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("errores", List.of("Error al registrar: " + e.getMessage()));
            return "auth/registro";
        }
    }
}
