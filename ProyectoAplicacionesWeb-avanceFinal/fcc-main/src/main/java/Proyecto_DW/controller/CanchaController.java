package Proyecto_DW.controller;

import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import Proyecto_DW.domain.Cancha;
import Proyecto_DW.domain.Usuario;
import Proyecto_DW.service.CanchaService;
import Proyecto_DW.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * CanchaController
 * Maneja la gestión de canchas disponibles en el sistema
 */
@Controller
@RequestMapping("/canchas")
public class CanchaController {

    private final CanchaService canchaService;
    private final UsuarioService usuarioService;

    @Autowired
    private MessageSource messageSource;

    public CanchaController(CanchaService canchaService, UsuarioService usuarioService) {
        this.canchaService = canchaService;
        this.usuarioService = usuarioService;
    }

    private Usuario getUsuarioActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.getUsuarioByEmail(email).orElse(null);
    }

    private boolean esVendedorOAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_VENDEDOR"));
    }

    /**
     * Listado de canchas disponibles
     */
    @GetMapping("/listado")
    public String listado(Model model, HttpSession session) {
        var canchas = canchaService.getCanchas(true);
        model.addAttribute("canchas", canchas);
        model.addAttribute("totalCanchas", canchas.size());
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
        return "canchas/listaCanchas";
    }

    /**
     * Búsqueda de canchas por query parameter
     * GET /canchas/buscar?q=...
     */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "q", required = false, defaultValue = "") String q, Model model, HttpSession session) {
        var canchas = canchaService.getCanchas(true);

        if (q != null && !q.trim().isEmpty()) {
            // Buscar por nombre o ubicación
            var canchasPorNombre = canchaService.buscarPorNombre(q);
            if (!canchasPorNombre.isEmpty()) {
                canchas = canchasPorNombre;
            } else {
                var canchasPorUbicacion = canchaService.buscarPorUbicacion(q);
                if (!canchasPorUbicacion.isEmpty()) {
                    canchas = canchasPorUbicacion;
                }
            }
        }

        model.addAttribute("canchas", canchas);
        model.addAttribute("totalCanchas", canchas.size());
        model.addAttribute("busqueda", q);
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
        return "canchas/listaCanchas";
    }

    /**
     * Detalles de una cancha específica (público, la reserva requiere autenticación)
     */
    @GetMapping("/detalles/{idCancha}")
    public String detalles(@PathVariable("idCancha") Integer idCancha, Model model, RedirectAttributes redirectAttributes) {
        Optional<Cancha> canchaOpt = canchaService.getCancha(idCancha);
        if (canchaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                messageSource.getMessage("cancha.error.noexiste", null, Locale.getDefault()));
            return "redirect:/canchas/listado";
        }
        model.addAttribute("cancha", canchaOpt.get());
        return "canchas/detalleCancha";
    }

    /**
     * Mis canchas (VENDEDOR y ADMIN)
     */
    @GetMapping("/mis-canchas")
    public String misCanchas(Model model) {
        Usuario usuario = getUsuarioActual();
        if (usuario == null) return "redirect:/auth/login";
        var canchas = canchaService.getCanchasByVendedor(usuario.getIdUsuario());
        model.addAttribute("canchas", canchas);
        model.addAttribute("totalCanchas", canchas.size());
        model.addAttribute("usuario", usuario);
        return "canchas/misCanchas";
    }

    /**
     * Formulario para crear una nueva cancha (solo administrador)
     */
    @GetMapping("/crear")
    public String crear(Model model) {
        model.addAttribute("cancha", new Cancha());
        return "canchas/crear";
    }

    /**
     * Guardar una nueva cancha (ADMIN o VENDEDOR)
     */
    @PostMapping("/guardar")
    public String guardar(@Valid Cancha cancha, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = getUsuarioActual();
            if (usuario != null) {
                cancha.setVendedor(usuario);
            }
            canchaService.save(cancha);
            redirectAttributes.addFlashAttribute("mensaje",
                messageSource.getMessage("cancha.guardada", null, Locale.getDefault()));
            return "redirect:/canchas/mis-canchas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                messageSource.getMessage("cancha.error.guardar", null, Locale.getDefault()));
            return "redirect:/canchas/crear";
        }
    }

    /**
     * Formulario para modificar una cancha (solo administrador)
     */
    @GetMapping("/modificar/{idCancha}")
    public String modificar(@PathVariable("idCancha") Integer idCancha, Model model, RedirectAttributes redirectAttributes) {
        Optional<Cancha> canchaOpt = canchaService.getCancha(idCancha);
        if (canchaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                messageSource.getMessage("cancha.error.noexiste", null, Locale.getDefault()));
            return "redirect:/canchas/listado";
        }
        model.addAttribute("cancha", canchaOpt.get());
        return "canchas/modificar";
    }

    /**
     * Actualizar una cancha existente (ADMIN o VENDEDOR dueño)
     */
    @PostMapping("/actualizar")
    public String actualizar(@Valid Cancha cancha, RedirectAttributes redirectAttributes) {
        try {
            // Preserve vendedor from the original cancha — form cannot bind nested objects
            canchaService.getCancha(cancha.getIdCancha()).ifPresent(original -> {
                if (cancha.getVendedor() == null) {
                    cancha.setVendedor(original.getVendedor());
                }
            });
            canchaService.update(cancha);
            redirectAttributes.addFlashAttribute("mensaje",
                messageSource.getMessage("cancha.actualizada", null, Locale.getDefault()));
            return "redirect:/canchas/mis-canchas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                messageSource.getMessage("cancha.error.actualizar", null, Locale.getDefault()));
            return "redirect:/canchas/modificar/" + cancha.getIdCancha();
        }
    }

    /**
     * Eliminar una cancha (solo administrador)
     */
    @PostMapping("/eliminar")
    public String eliminar(Integer idCancha, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String detalle = "cancha.eliminada";
        try {
            canchaService.delete(idCancha);
        } catch (IllegalArgumentException e) {
            titulo = "error";
            detalle = "cancha.error.noexiste";
        } catch (IllegalStateException e) {
            titulo = "error";
            detalle = "cancha.error.tieneReservas";
        } catch (Exception e) {
            titulo = "error";
            detalle = "cancha.error.general";
        }
        redirectAttributes.addFlashAttribute(titulo,
            messageSource.getMessage(detalle, null, Locale.getDefault()));
        return "redirect:/canchas/mis-canchas";
    }
}
