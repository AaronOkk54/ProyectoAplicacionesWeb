package Proyecto_DW.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad HistorialReserva
 * Mantiene un registro de todos los cambios realizados en las reservas.
 * Permite auditoría y trazabilidad de las modificaciones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "historial_reserva")
public class HistorialReserva implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer idHistorial;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "La acción no puede estar vacía.")
    @Size(max = 100, message = "La acción no puede exceder 100 caracteres.")
    private String accion; // CREADA, CONFIRMADA, CANCELADA, MODIFICADA, COMPLETADA

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El estado anterior no puede estar vacío.")
    private String estadoAnterior; // Estado previo de la reserva

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El estado nuevo no puede estar vacío.")
    private String estadoNuevo; // Nuevo estado de la reserva

    @Column(columnDefinition = "TEXT")
    private String descripcion; // Detalles del cambio realizado

    @Column(nullable = false, length = 100)
    private String usuario; // Usuario que realizó la acción (puede ser ADMIN o EMAIL del usuario)

    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    private LocalDateTime fechaCambio;

    @Column(columnDefinition = "TEXT")
    private String cambiosDetalles; // JSON o texto con los cambios específicos

    @PrePersist
    protected void onCreate() {
        fechaCambio = LocalDateTime.now();
    }
}
