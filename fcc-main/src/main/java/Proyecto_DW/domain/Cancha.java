package Proyecto_DW.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Entidad Cancha
 * Representa las canchas de fútbol disponibles para reservar.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cancha")
public class Cancha implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cancha")
    private Integer idCancha;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre de la cancha no puede estar vacío.")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres.")
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "La ubicación no puede estar vacía.")
    private String ubicacion;

    @Column(name = "precio_hora", nullable = false, precision = 12, scale = 2)
    @NotNull(message = "El precio por hora no puede estar vacío.")
    @DecimalMin(value = "0.01", inclusive = true, message = "El precio debe ser mayor a 0.")
    private BigDecimal precioHora;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El estado no puede estar vacío.")
    private String estado = "DISPONIBLE"; // DISPONIBLE, MANTENIMIENTO, CLAUSURADA

    @Column(name = "capacidad")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0.")
    private Integer capacidad; // Número de jugadores

    @Column(columnDefinition = "TEXT")
    private String caracteristicas; // Césped artificial, iluminación, baños, etc.

    @Column(name = "horario_apertura", length = 5)
    private String horarioApertura; // HH:MM

    @Column(name = "horario_cierre", length = 5)
    private String horarioCierre; // HH:MM

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "id_vendedor")
    private Usuario vendedor;

    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reserva> reservas = new HashSet<>();

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
