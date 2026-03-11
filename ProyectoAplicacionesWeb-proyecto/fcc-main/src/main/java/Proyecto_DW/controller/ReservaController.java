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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    /**
     * Listado de mis reservas (usuario autenticado)
     */
    @GetMapping("/mis-reservas")
    public String misReservas(Model model) {
        // Se obtendría del contexto de seguridad
        var reservas = reservaService.getReservasPorUsuario(1); // idUsuario del contexto
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
            // Se establecería el usuario del contexto de seguridad
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
        model.addAttribute("reserva", reservaOpt.get());
        return "reservas/detalleReserva";
    }

    /**
     * Confirmar una reserva
     */
    @PostMapping("/confirmar/{idReserva}")
    public String confirmar(@PathVariable("idReserva") Integer idReserva, RedirectAttributes redirectAttributes) {
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
     * Cancelar una reserva
     */
    @PostMapping("/cancelar/{idReserva}")
    public String cancelar(@PathVariable("idReserva") Integer idReserva, RedirectAttributes redirectAttributes) {
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
        model.addAttribute("reserva", reservaOpt.get());
        model.addAttribute("historiales", reservaOpt.get().getHistoriales());
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
