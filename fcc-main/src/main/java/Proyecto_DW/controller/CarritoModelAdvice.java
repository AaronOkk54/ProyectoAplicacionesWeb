/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Proyecto_DW.controller;

import Proyecto_DW.service.CarritoService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 *
 * @author Alfaro
 */
@ControllerAdvice
public class CarritoModelAdvice {

    private final CarritoService carritoService;

    public CarritoModelAdvice(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @ModelAttribute("carritoCount")
    public int carritoCount() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return carritoService.getCantidad();
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}