package Proyecto_DW.controller;

import Proyecto_DW.domain.Usuario;
import Proyecto_DW.service.CorreoService;
import Proyecto_DW.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final CorreoService correoService;

    public AuthController(UsuarioService usuarioService, CorreoService correoService) {
        this.usuarioService = usuarioService;
        this.correoService = correoService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/inicioSesion";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@ModelAttribute("usuario") Usuario usuario,
                            @RequestParam(defaultValue = "CLIENTE") String rol,
                            Model model) {
        try {
            usuarioService.registrar(usuario, rol);
            enviarCorreoBienvenida(usuario.getNombre(), usuario.getEmail());
            return "redirect:/auth/login?registrado=true";
        } catch (Exception e) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("errores", List.of("Error al registrar: " + e.getMessage()));
            return "auth/registro";
        }
    }

    private void enviarCorreoBienvenida(String nombre, String email) {
        try {
            String html = "<!DOCTYPE html><html><body style=\"font-family: Arial, sans-serif; background: #f5f8f5; margin: 0; padding: 20px;\">"
                + "<div style=\"max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1);\">"
                + "<div style=\"background: #111827; padding: 32px; text-align: center;\">"
                + "<h1 style=\"color: #0df20d; margin: 0; font-size: 26px;\">ReservaCanchaCR</h1>"
                + "<p style=\"color: #9ca3af; margin: 8px 0 0 0; font-size: 14px;\">La plataforma de reservas de canchas de Costa Rica</p>"
                + "</div>"
                + "<div style=\"padding: 32px;\">"
                + "<h2 style=\"color: #111811; margin-top: 0;\">¡Bienvenido, " + nombre + "!</h2>"
                + "<p style=\"color: #608a60; line-height: 1.7; font-size: 15px;\">Tu cuenta ha sido creada exitosamente. Ya puedes explorar y reservar las mejores canchas de fútbol en Costa Rica.</p>"
                + "<div style=\"background: #f5f8f5; border-radius: 8px; padding: 20px; margin: 24px 0; border-left: 4px solid #0df20d;\">"
                + "<h3 style=\"margin: 0 0 12px 0; color: #111811; font-size: 14px;\">¿Qué puedes hacer?</h3>"
                + "<ul style=\"margin: 0; padding: 0 0 0 20px; color: #608a60; font-size: 14px; line-height: 2;\">"
                + "<li>Buscar canchas disponibles en tu zona</li>"
                + "<li>Hacer reservas individuales o usar el carrito</li>"
                + "<li>Gestionar y cancelar tus reservas fácilmente</li>"
                + "<li>Ver el historial de todas tus reservas</li>"
                + "</ul></div>"
                + "<div style=\"text-align: center; margin-top: 28px;\">"
                + "<a href=\"http://localhost:85/canchas/listado\" style=\"background: #0df20d; color: #111811; padding: 14px 36px; border-radius: 8px; text-decoration: none; font-weight: bold; font-size: 14px; display: inline-block;\">Explorar Canchas</a>"
                + "</div></div>"
                + "<div style=\"background: #f9fafb; padding: 20px; text-align: center; border-top: 1px solid #e5e7eb;\">"
                + "<p style=\"margin: 0; color: #9ca3af; font-size: 12px;\">&copy; 2024 ReservaCanchaCR. Todos los derechos reservados.</p>"
                + "</div></div></body></html>";
            correoService.enviarCorreoHtml(email, "¡Bienvenido a ReservaCanchaCR!", html);
        } catch (Exception ignored) {
            // No falla el registro si el correo falla
        }
    }
}
