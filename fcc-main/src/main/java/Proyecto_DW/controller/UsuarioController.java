package Proyecto_DW.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        usuarioService.getUsuarioByEmail(email).ifPresent(u -> model.addAttribute("usuario", u));
        return "usuario/perfil";
    }

    /**
     * Configuración del usuario
     */
    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        usuarioService.getUsuarioByEmail(email).ifPresent(u -> model.addAttribute("usuario", u));
        return "usuario/configuracion";
    }

    /**
     * Pagos del usuario
     */
    @GetMapping("/pagos")
    public String pagos(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        usuarioService.getUsuarioByEmail(email).ifPresent(u -> model.addAttribute("usuario", u));
        return "usuario/pagos";
    }

    /**
     * Actualizar perfil del usuario autenticado
     */
    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam("idUsuario") Integer idUsuario,
                                   @RequestParam("nombre") String nombre,
                                   @RequestParam(value = "telefono", required = false) String telefono,
                                   RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(idUsuario);
            usuario.setNombre(nombre);
            usuario.setTelefono(telefono);
            usuarioService.update(usuario);
            redirectAttributes.addFlashAttribute("todoOk", "Perfil actualizado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
        }
        return "redirect:/usuario/perfil";
    }

    /**
     * Listar usuarios (ADMIN)
     */
    @GetMapping("/listado")
    public String listado(Model model) {
        var usuarios = usuarioService.getUsuarios();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());
        return "usuario/listado";
    }

    /**
     * Activar usuario (ADMIN)
     */
    @PostMapping("/activar")
    public String activar(@RequestParam("idUsuario") Integer idUsuario,
                          RedirectAttributes redirectAttributes) {
        try {
            usuarioService.activar(idUsuario);
            redirectAttributes.addFlashAttribute("todoOk", "Usuario activado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al activar el usuario: " + e.getMessage());
        }
        return "redirect:/usuario/listado";
    }

    /**
     * Desactivar usuario (ADMIN)
     */
    @PostMapping("/desactivar")
    public String desactivar(@RequestParam("idUsuario") Integer idUsuario,
                             RedirectAttributes redirectAttributes) {
        try {
            usuarioService.desactivar(idUsuario);
            redirectAttributes.addFlashAttribute("todoOk", "Usuario desactivado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al desactivar el usuario: " + e.getMessage());
        }
        return "redirect:/usuario/listado";
    }

    /**
     * Eliminar usuario (ADMIN)
     */
    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("idUsuario") Integer idUsuario,
                           RedirectAttributes redirectAttributes) {
        try {
            usuarioService.delete(idUsuario);
            redirectAttributes.addFlashAttribute("todoOk", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/usuario/listado";
    }

    /**
     * Editar usuario (ADMIN) - guarda nombre y telefono
     */
    @PostMapping("/editar")
    public String editarUsuario(@RequestParam("idUsuario") Integer idUsuario,
                                @RequestParam("nombre") String nombre,
                                @RequestParam(value = "telefono", required = false) String telefono,
                                RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(idUsuario);
            usuario.setNombre(nombre);
            usuario.setTelefono(telefono);
            usuarioService.update(usuario);
            redirectAttributes.addFlashAttribute("todoOk", "Usuario actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el usuario: " + e.getMessage());
        }
        return "redirect:/usuario/listado";
    }
}
