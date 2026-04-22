/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Proyecto_DW.controller;

import Proyecto_DW.domain.Cancha;
import Proyecto_DW.domain.CarritoItem;
import Proyecto_DW.domain.Reserva;
import Proyecto_DW.domain.Usuario;
import Proyecto_DW.service.CanchaService;
import Proyecto_DW.service.CarritoService;
import Proyecto_DW.service.ReservaService;
import Proyecto_DW.service.UsuarioService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author Alfaro
 */
@Controller
@RequestMapping("/carrito")
public class CarritoController {

    private final CarritoService carritoService;
    private final CanchaService canchaService;
    private final UsuarioService usuarioService;
    private final ReservaService reservaService;

    public CarritoController(CarritoService carritoService,
                             CanchaService canchaService,
                             UsuarioService usuarioService,
                             ReservaService reservaService) {
        this.carritoService = carritoService;
        this.canchaService = canchaService;
        this.usuarioService = usuarioService;
        this.reservaService = reservaService;
    }

    private Usuario getUsuarioActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.getUsuarioByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));
    }

    @GetMapping
    public String verCarrito(Model model) {
        model.addAttribute("items", carritoService.getItems());
        model.addAttribute("total", carritoService.getTotal());
        model.addAttribute("cantidad", carritoService.getCantidad());
        model.addAttribute("usuario", getUsuarioActual());
        return "carrito/carrito";
    }

    @GetMapping("/preparar/{idCancha}")
    public String prepararAgregar(@PathVariable Integer idCancha,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Optional<Cancha> canchaOpt = canchaService.getCancha(idCancha);
        if (canchaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cancha no encontrada.");
            return "redirect:/canchas/listado";
        }
        Cancha cancha = canchaOpt.get();
        if (!"DISPONIBLE".equals(cancha.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Esta cancha no está disponible para reservar.");
            return "redirect:/canchas/detalles/" + idCancha;
        }
        model.addAttribute("cancha", cancha);
        model.addAttribute("usuario", getUsuarioActual());
        return "carrito/agregarAlCarrito";
    }

    @PostMapping("/agregar")
    public String agregar(@RequestParam Integer idCancha,
                          @RequestParam String fecha,
                          @RequestParam String horaInicio,
                          @RequestParam String horaFin,
                          @RequestParam(defaultValue = "EFECTIVO") String metodoPago,
                          RedirectAttributes redirectAttributes) {
        Optional<Cancha> canchaOpt = canchaService.getCancha(idCancha);
        if (canchaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cancha no encontrada.");
            return "redirect:/canchas/listado";
        }

        Cancha cancha = canchaOpt.get();
        LocalDate fechaReserva = LocalDate.parse(fecha);
        LocalTime inicio = LocalTime.parse(horaInicio);
        LocalTime fin = LocalTime.parse(horaFin);

        if (!fin.isAfter(inicio)) {
            redirectAttributes.addFlashAttribute("error", "La hora de fin debe ser posterior a la hora de inicio.");
            return "redirect:/carrito/preparar/" + idCancha;
        }
        if (fechaReserva.isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("error", "La fecha no puede ser en el pasado.");
            return "redirect:/carrito/preparar/" + idCancha;
        }

        long minutos = ChronoUnit.MINUTES.between(inicio, fin);
        BigDecimal horasFraccion = BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal montoTotal = cancha.getPrecioHora().multiply(horasFraccion).setScale(2, RoundingMode.HALF_UP);

        CarritoItem item = new CarritoItem();
        item.setIdCancha(cancha.getIdCancha());
        item.setNombreCancha(cancha.getNombre());
        item.setUbicacionCancha(cancha.getUbicacion());
        item.setPrecioHora(cancha.getPrecioHora());
        item.setFecha(fechaReserva);
        item.setHoraInicio(inicio);
        item.setHoraFin(fin);
        item.setMontoTotal(montoTotal);
        item.setMetodoPago(metodoPago);

        carritoService.agregarItem(item);
        redirectAttributes.addFlashAttribute("todoOk", "Cancha \"" + cancha.getNombre() + "\" agregada al carrito.");
        return "redirect:/carrito";
    }

    @PostMapping("/eliminar/{itemId}")
    public String eliminar(@PathVariable String itemId, RedirectAttributes redirectAttributes) {
        carritoService.eliminarItem(itemId);
        redirectAttributes.addFlashAttribute("todoOk", "Item eliminado del carrito.");
        return "redirect:/carrito";
    }

    @PostMapping("/vaciar")
    public String vaciar(RedirectAttributes redirectAttributes) {
        carritoService.vaciar();
        redirectAttributes.addFlashAttribute("todoOk", "Carrito vaciado.");
        return "redirect:/carrito";
    }

    @PostMapping("/checkout")
    public String checkout(RedirectAttributes redirectAttributes) {
        List<CarritoItem> items = carritoService.getItems();
        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El carrito está vacío.");
            return "redirect:/carrito";
        }

        Usuario usuario = getUsuarioActual();
        int creadas = 0;
        int errores = 0;

        for (CarritoItem item : items) {
            Optional<Cancha> canchaOpt = canchaService.getCancha(item.getIdCancha());
            if (canchaOpt.isEmpty()) {
                errores++;
                continue;
            }
            Reserva reserva = new Reserva();
            reserva.setUsuario(usuario);
            reserva.setCancha(canchaOpt.get());
            reserva.setFecha(item.getFecha());
            reserva.setHoraInicio(item.getHoraInicio());
            reserva.setHoraFin(item.getHoraFin());
            reserva.setMontoTotal(item.getMontoTotal());
            reserva.setMetodoPago(item.getMetodoPago());
            reserva.setEstado("PENDIENTE");
            try {
                reservaService.save(reserva);
                creadas++;
            } catch (Exception e) {
                errores++;
            }
        }

        carritoService.vaciar();
        if (creadas > 0) {
            redirectAttributes.addFlashAttribute("todoOk",
                    creadas + " reserva(s) creada(s) exitosamente." + (errores > 0 ? " " + errores + " no pudieron procesarse." : ""));
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo crear ninguna reserva. Verifica la disponibilidad.");
        }
        return "redirect:/reserva/mis-reservas";
    }
}

