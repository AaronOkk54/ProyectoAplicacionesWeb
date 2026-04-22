package Proyecto_DW.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Reserva
 * Representa una reserva realizada por un usuario en una cancha específica.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reserva")
public class Reserva implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Integer idReserva;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cancha", nullable = false)
    private Cancha cancha;

    @Column(nullable = false)
    @NotNull(message = "La fecha de la reserva no puede estar vacía.")
    @FutureOrPresent(message = "La fecha debe ser hoy o posterior.")
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    @NotNull(message = "La hora de inicio no puede estar vacía.")
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    @NotNull(message = "La hora de fin no puede estar vacía.")
    private LocalTime horaFin;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El estado no puede estar vacío.")
    private String estado = "PENDIENTE"; // PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    @NotNull(message = "El monto total no puede estar vacío.")
    @DecimalMin(value = "0.01", inclusive = true, message = "El monto debe ser mayor a 0.")
    private BigDecimal montoTotal;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago; // EFECTIVO, TARJETA, TRANSFERENCIA, etc.

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HistorialReserva> historiales = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
