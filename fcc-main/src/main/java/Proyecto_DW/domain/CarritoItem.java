/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Proyecto_DW.domain;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
/**
 *
 * @author Alfaro
 */
public class CarritoItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String itemId;
    private Integer idCancha;
    private String nombreCancha;
    private String ubicacionCancha;
    private BigDecimal precioHora;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private BigDecimal montoTotal;
    private String metodoPago;

    public CarritoItem() {
        this.itemId = UUID.randomUUID().toString();
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public Integer getIdCancha() { return idCancha; }
    public void setIdCancha(Integer idCancha) { this.idCancha = idCancha; }

    public String getNombreCancha() { return nombreCancha; }
    public void setNombreCancha(String nombreCancha) { this.nombreCancha = nombreCancha; }

    public String getUbicacionCancha() { return ubicacionCancha; }
    public void setUbicacionCancha(String ubicacionCancha) { this.ubicacionCancha = ubicacionCancha; }

    public BigDecimal getPrecioHora() { return precioHora; }
    public void setPrecioHora(BigDecimal precioHora) { this.precioHora = precioHora; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
}

