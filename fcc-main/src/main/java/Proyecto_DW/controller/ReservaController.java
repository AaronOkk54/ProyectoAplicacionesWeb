package Proyecto_DW.controller;

import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import Proyecto_DW.domain.Usuario;

import Proyecto_DW.domain.Cancha;
import Proyecto_DW.domain.Reserva;
import Proyecto_DW.service.CanchaService;
import Proyecto_DW.service.ReservaService;
import Proyecto_DW.service.UsuarioService;
import jakarta.validation.Valid;

/**
 * ReservaController
 * Maneja la gestión de reservas de canchas
 */
@Controller
@RequestMapping("/reserva")
public class ReservaController {

    private final ReservaService reservaService;
    private final CanchaService canchaService;
    private final UsuarioService usuarioService;

    @Autowired
    private MessageSource messageSource;

    public ReservaController(ReservaService reservaService, CanchaService canchaService, UsuarioService usuarioService) {
        this.reservaService = reservaService;
        this.canchaService = canchaService;
        this.usuarioService = usuarioService;
    }

    // ── Helpers de seguridad ─────────────────────────────────────────────────

    private Usuario getUsuarioActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.getUsuarioByEmail(email).orElse(null);
    }

    private boolean esAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // ── Endpoints ────────────────────────────────────────────────────────────

    /**
     * Listado de mis reservas (usuario autenticado)
     */
    @GetMapping("/mis-reservas")
    public String misReservas(Model model) {
        var usuario = getUsuarioActual();
        if (usuario == null) return "redirect:/auth/login";
        var reservas = reservaService.getReservasPorUsuario(usuario.getIdUsuario());
        model.addAttribute("usuario", usuario);
        model.addAttribute("reservas", reservas);
        model.addAttribute("totalReservas", reservas.size());
        return "reservas/misReservas";
    }

    /**
     * Formulario para crear una nueva reserva
     */
    @GetMapping("/crear/{idCancha}")
    public String crear(@PathVariable("idCancha") Integer idCancha, Model model, RedirectAttributes redirectAttributes) {
        Optional<Cancha> canchaOpt = canchaService.getCancha(idCancha);
        if (canchaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                messageSource.getMessage("cancha.error.noexiste", null, Locale.getDefault()));
            return "redirect:/canchas/listado";
        }
        model.addAttribute("cancha", canchaOpt.get());
        model.addAttribute("reserva", new Reserva());
        return "reservas/crear";
    }

    /**
     * Procesar la creación de una nueva reserva
     */
    @PostMapping("/guardar")
    public String guardar(@Valid Reserva reserva, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuarioActual = getUsuarioActual();
            if (usuarioActual == null) return "redirect:/auth/login";
            reserva.setUsuario(usuarioActual);
            reservaService.save(reserva);
            redirectAttributes.addFlashAttribute("todoOk", 
                messageSource.getMessage("reserva.creada", null, Locale.getDefault()));
            return "redirect:/reserva/mis-reservas";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", 
                messageSource.getMessage("reserva.error.canchaOcupada", null, Locale.getDefault()));
            return "redirect:/reserva/crear/" + reserva.getCancha().getIdCancha();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                messageSource.getMessage("reserva.error.guardar", null, Locale.getDefault()));
            return "redirect:/reserva/crear/" + reserva.getCancha().getIdCancha();
        }
    }

    /**
     * Detalles de una reserva específica
     */
    @GetMapping("/detalles/{idReserva}")
    public String detalles(@PathVariable("idReserva") Integer idReserva, Model model, RedirectAttributes redirectAttributes) {
        Optional<Reserva> reservaOpt = reservaService.getReserva(idReserva);
        if (reservaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                messageSource.getMessage("reserva.error.noexiste", null, Locale.getDefault()));
            return "redirect:/reserva/mis-reservas";
        }
        Reserva reserva = reservaOpt.get();
        Usuario usuarioActual = getUsuarioActual();
        if (!esAdmin() && (usuarioActual == null || !reserva.getUsuario().getIdUsuario().equals(usuarioActual.getIdUsuario()))) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver esta reserva.");
            return "redirect:/reserva/mis-reservas";
        }
        model.addAttribute("reserva", reserva);
        return "reservas/detalleReserva";
    }

    /**
     * Confirmar una reserva (solo ADMIN)
     */
    @PostMapping("/confirmar/{idReserva}")
    public String confirmar(@PathVariable("idReserva") Integer idReserva, RedirectAttributes redirectAttributes) {
        if (!esAdmin()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para confirmar reservas.");
            return "redirect:/reserva/detalles/" + idReserva;
        }
        try {
            reservaService.confirmar(idReserva);
            redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("reserva.confirmada", null, Locale.getDefault()));
            return "redirect:/reserva/detalles/" + idReserva;
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error",
                messageSource.getMessage("reserva.error.noConfirmable", null, Locale.getDefault()));
            return "redirect:/reserva/detalles/" + idReserva;
        }
    }

    /**
     * Cancelar una reserva (solo el dueño o ADMIN)
     */
    @PostMapping("/cancelar/{idReserva}")
    public String cancelar(@PathVariable("idReserva") Integer idReserva, RedirectAttributes redirectAttributes) {
        Optional<Reserva> reservaOpt = reservaService.getReserva(idReserva);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            Usuario usuarioActualCancel = getUsuarioActual();
            if (!esAdmin() && (usuarioActualCancel == null || !reserva.getUsuario().getIdUsuario().equals(usuarioActualCancel.getIdUsuario()))) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para cancelar esta reserva.");
                return "redirect:/reserva/mis-reservas";
            }
        }
        String titulo = "todoOk";
        String detalle = "reserva.cancelada";
        try {
            reservaService.cancelar(idReserva);
        } catch (IllegalArgumentException e) {
            titulo = "error";
            detalle = "reserva.error.noexiste";
        } catch (IllegalStateException e) {
            titulo = "error";
            detalle = "reserva.error.noCancelable";
        } catch (Exception e) {
            titulo = "error";
            detalle = "reserva.error.general";
        }
        redirectAttributes.addFlashAttribute(titulo, 
            messageSource.getMessage(detalle, null, Locale.getDefault()));
        return "redirect:/reserva/mis-reservas";
    }

    /**
     * Historial de una reserva (cambios realizados)
     */
    @GetMapping("/historial/{idReserva}")
    public String historial(@PathVariable("idReserva") Integer idReserva, Model model, RedirectAttributes redirectAttributes) {
        Optional<Reserva> reservaOpt = reservaService.getReserva(idReserva);
        if (reservaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                messageSource.getMessage("reserva.error.noexiste", null, Locale.getDefault()));
            return "redirect:/reserva/mis-reservas";
        }
        Reserva reserva = reservaOpt.get();
        Usuario usuarioActualHist = getUsuarioActual();
        if (!esAdmin() && (usuarioActualHist == null || !reserva.getUsuario().getIdUsuario().equals(usuarioActualHist.getIdUsuario()))) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver el historial de esta reserva.");
            return "redirect:/reserva/mis-reservas";
        }
        model.addAttribute("reserva", reserva);
        model.addAttribute("historiales", reserva.getHistoriales());
        model.addAttribute("usuario", usuarioActualHist);
        return "reservas/historialReservas";
    }

    /**
     * Listado de todas las reservas (redirige a mis-reservas)
     */
    @GetMapping("/listado")
    public String listado(Model model) {
        return "redirect:/reserva/mis-reservas";
    }
}
