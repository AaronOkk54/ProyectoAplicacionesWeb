package Proyecto_DW.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import Proyecto_DW.domain.Cancha;
import Proyecto_DW.service.CanchaService;

/**
 * HomeController
 * Maneja las rutas de la página de inicio y vista general del sistema
 */
@Controller
public class HomeController {

    private final CanchaService canchaService;

    public HomeController(CanchaService canchaService) {
        this.canchaService = canchaService;
    }

    /**
     * Página de inicio principal
     */
    @GetMapping("/")
    public String index(Model model) {
        var canchas = canchaService.getCanchas(true);
        model.addAttribute("canchas", canchas);
        model.addAttribute("canchasDestacadas", canchas.stream().limit(3).toList());
        return "index";
    }

    /**
     * Página de bienvenida para usuarios autenticados
     */
    @GetMapping("/inicioSesion")
    public String dashboard(Model model) {
        return "inicioSesion";
    }

    /**
     * Página Sobre Nosotros
     */
    @GetMapping("/sobre-nosotros")
    public String sobreNosotros() {
        return "sobreNosotros";
    }

    @GetMapping("/carreras")
    public String carreras() {
        return "carreras";
    }

    @GetMapping("/blog")
    public String blog() {
        return "blog";
    }

    @GetMapping("/ayuda")
    public String centroAyuda() {
        return "centroAyuda";
    }

    @GetMapping("/terminos")
    public String terminos() {
        return "terminos";
    }

    @GetMapping("/privacidad")
    public String privacidad() {
        return "privacidad";
    }

    @GetMapping("/lista-cancha")
    public String listaCancha() {
        return "listaCancha";
    }
}
