package Proyecto_DW.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import Proyecto_DW.service.ReservaService;

@Controller
public class RouteController {

    private final ReservaService reservaService;

    public RouteController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    

    @GetMapping("/reservas/misReservas")
    public String reservasMisReservasRedirect() {
        return "redirect:/reserva/mis-reservas";
    }

    @GetMapping("/reservas/activas")
    public String reservasActivasRedirect() {
        return "redirect:/reserva/listado";
    }

    @GetMapping("/reservas/nueva")
    public String reservasNuevaRedirect(@RequestParam(value = "canchaId", required = false) Integer canchaId) {
        if (canchaId != null) {
            return "redirect:/reserva/crear/" + canchaId;
        }
        return "redirect:/reserva/listado";
    }

    @GetMapping("/reservas/detalle")
    public String reservasDetalleRedirect(@RequestParam(value = "id", required = true) Integer id) {
        return "redirect:/reserva/detalles/" + id;
    }

    @GetMapping("/canchas/detalle")
    public String canchasDetalleRedirect(@RequestParam(value = "id", required = true) Integer id) {
        return "redirect:/canchas/detalles/" + id;
    }

    @GetMapping("/reservas/pagar")
    public String reservasPagarRedirect(@RequestParam(value = "id", required = true) Integer id) {
        return "redirect:/reserva/detalles/" + id;
    }

    @GetMapping("/reservas/repetir")
    public String reservasRepetirRedirect(@RequestParam(value = "id", required = true) Integer id) {
        return "redirect:/reserva/detalles/" + id;
    }

    @GetMapping("/reservas/comprobante")
    public String reservasComprobanteRedirect(@RequestParam(value = "id", required = true) Integer id) {
        return "redirect:/reserva/detalles/" + id;
    }
    @GetMapping("/reservas/historial")
    public String reservasHistorialRedirect(@RequestParam(value = "id", required = false) Integer id) {
        if (id != null) {
            return "redirect:/reserva/historial/" + id;
        }
        return "redirect:/reserva/listado";
    }

    @GetMapping("/reservas/cancelar")
    public String reservasCancelarGet(@RequestParam(value = "id", required = true) Integer id, RedirectAttributes redirectAttributes) {
        try {
            reservaService.cancelar(id);
            redirectAttributes.addFlashAttribute("todoOk", "Reserva cancelada");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No fue posible cancelar la reserva");
        }
        return "redirect:/reserva/mis-reservas";
    }

    @GetMapping("/usuario/inicio")
    public String usuarioInicio() {
        return "redirect:/usuario/inicio";
    }
}
