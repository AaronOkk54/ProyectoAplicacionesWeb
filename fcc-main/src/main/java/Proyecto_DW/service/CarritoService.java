/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Proyecto_DW.service;
import Proyecto_DW.domain.CarritoItem;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

/**
 *
 * @author Alfaro
 */
@Service
@SessionScope
public class CarritoService implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<CarritoItem> items = new ArrayList<>();

    public void agregarItem(CarritoItem item) {
        items.add(item);
    }

    public void eliminarItem(String itemId) {
        items.removeIf(item -> item.getItemId().equals(itemId));
    }

    public void vaciar() {
        items.clear();
    }

    public List<CarritoItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(CarritoItem::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getCantidad() {
        return items.size();
    }
}

