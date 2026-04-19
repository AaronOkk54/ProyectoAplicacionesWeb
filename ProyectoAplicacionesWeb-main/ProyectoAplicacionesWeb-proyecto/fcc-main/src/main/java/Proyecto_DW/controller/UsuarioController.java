package Proyecto_DW.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import Proyecto_DW.domain.Usuario;
import Proyecto_DW.service.CanchaService;
import Proyecto_DW.service.ReservaService;
import Proyecto_DW.service.UsuarioService;

/**
 * UsuarioController
 * Proporciona vistas de usuario. La funcionalidad de registro y actualización ha sido deshabilitada.
 */
@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final CanchaService canchaService;
    private final ReservaService reservaService;

    public UsuarioController(UsuarioService usuarioService, CanchaService canchaService, ReservaService reservaService) {
        this.usuarioService = usuarioService;
        this.canchaService = canchaService;
        this.reservaService = reservaService;
    }

    /**
     * Formulario de registro
     */
    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/registro";
    }

    /**
     * Página de inicio del usuario (requiere autenticación)
     */
    @GetMapping("/inicio")
    public String inicio(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        usuarioService.getUsuarioByEmail(email).ifPresent(u -> model.addAttribute("usuario", u));
        var canchasDisponibles = canchaService.getCanchas(true);
        model.addAttribute("canchasDestacadas", canchasDisponibles.stream().limit(4).toList());
        model.addAttribute("totalCanchasDisponibles", canchasDisponibles.size());
        model.addAttribute("totalReservasActivas", reservaService.getReservasActivas().size());
        return "usuario/inicio";
    }

    /**
     * Formulario de login
     */
    @GetMapping("/login")
    public String login() {
        return "auth/inicioSesion";
    }

    /**
     * Perfil del usuario
     */
    @GetMapping("/perfil")
    public String perfil(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuario/perfil";
    }

    /**
     * Listar usuarios
     */
    @GetMapping("/listado")
    public String listado(Model model) {
        var usuarios = usuarioService.getUsuarios();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());
        return "usuario/listado";
    }
}
