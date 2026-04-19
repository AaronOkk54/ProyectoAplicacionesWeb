package Proyecto_DW.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Proyecto_DW.domain.HistorialReserva;
import Proyecto_DW.domain.Reserva;
import Proyecto_DW.repository.HistorialReservaRepository;
import Proyecto_DW.repository.ReservaRepository;

/**
 * ReservaService
 * Servicio para la gestión de reservas de canchas
 */
@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final HistorialReservaRepository historialReservaRepository;

    public ReservaService(ReservaRepository reservaRepository, HistorialReservaRepository historialReservaRepository) {
        this.reservaRepository = reservaRepository;
        this.historialReservaRepository = historialReservaRepository;
    }

    /**
     * Obtener todas las reservas
     */
    @Transactional(readOnly = true)
    public List<Reserva> getReservas() {
        return reservaRepository.findAll();
    }

    /**
     * Obtener una reserva por ID
     */
    @Transactional(readOnly = true)
    public Optional<Reserva> getReserva(Integer idReserva) {
        return reservaRepository.findById(idReserva);
    }

    /**
     * Obtener reservas de un usuario
     */
    @Transactional(readOnly = true)
    public List<Reserva> getReservasPorUsuario(Integer idUsuario) {
        // Esta implementación requiere inyección de UsuarioRepository
        // Por ahora retornamos lista vacía
        return reservaRepository.findAll().stream()
            .filter(r -> r.getUsuario().getIdUsuario().equals(idUsuario))
            .toList();
    }

    /**
     * Obtener reservas activas (no canceladas ni completadas)
     */
    @Transactional(readOnly = true)
    public List<Reserva> getReservasActivas() {
        return reservaRepository.findReservasActivas();
    }

    /**
     * Obtener reservas confirmadas
     */
    @Transactional(readOnly = true)
    public List<Reserva> getReservasConfirmadas() {
        return reservaRepository.findByEstadoOrderByFechaAsc("CONFIRMADA");
    }

    /**
     * Guardar una nueva reserva
     */
    @Transactional
    public Reserva save(Reserva reserva) {
        // Validar datos requeridos
        if (reserva.getUsuario() == null) {
            throw new IllegalArgumentException("El usuario es requerido.");
        }

        if (reserva.getCancha() == null) {
            throw new IllegalArgumentException("La cancha es requerida.");
        }

        if (reserva.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es requerida.");
        }

        if (reserva.getHoraInicio() == null || reserva.getHoraFin() == null) {
            throw new IllegalArgumentException("Las horas de inicio y fin son requeridas.");
        }

        // Verificar que la hora de fin sea posterior a la de inicio
        if (!reserva.getHoraFin().isAfter(reserva.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }

        // Verificar que la fecha no sea en el pasado
        if (reserva.getFecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden hacer reservas en fechas pasadas.");
        }

        // Verificar disponibilidad de la cancha
        if (existeConflicto(reserva)) {
            throw new IllegalStateException("La cancha no está disponible en el horario seleccionado.");
        }

        // Establecer estado por defecto
        if (reserva.getEstado() == null) {
            reserva.setEstado("PENDIENTE");
        }

        // Calcular monto total si no está establecido
        if (reserva.getMontoTotal() == null) {
            reserva.setMontoTotal(calcularMontoTotal(reserva));
        }

        // Guardar la reserva
        reserva = reservaRepository.save(reserva);

        // Registrar en historial
        registrarEnHistorial(reserva, "CREADA", null, reserva.getEstado(), "Reserva creada por el usuario");

        return reserva;
    }

    /**
     * Confirmar una reserva
     */
    @Transactional
    public void confirmar(Integer idReserva) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
        if (reservaOpt.isEmpty()) {
            throw new IllegalArgumentException("La reserva con ID " + idReserva + " no existe.");
        }

        Reserva reserva = reservaOpt.get();

        if (!reserva.getEstado().equals("PENDIENTE")) {
            throw new IllegalStateException("Solo se pueden confirmar reservas en estado PENDIENTE.");
        }

        String estadoAnterior = reserva.getEstado();
        reserva.setEstado("CONFIRMADA");
        reserva.setFechaConfirmacion(LocalDateTime.now());
        reservaRepository.save(reserva);

        // Registrar en historial
        registrarEnHistorial(reserva, "CONFIRMADA", estadoAnterior, reserva.getEstado(), "Reserva confirmada");
    }

    /**
     * Cancelar una reserva
     */
    @Transactional
    public void cancelar(Integer idReserva) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
        if (reservaOpt.isEmpty()) {
            throw new IllegalArgumentException("La reserva con ID " + idReserva + " no existe.");
        }

        Reserva reserva = reservaOpt.get();

        if (reserva.getEstado().equals("COMPLETADA") || reserva.getEstado().equals("CANCELADA")) {
            throw new IllegalStateException("No se puede cancelar una reserva en estado " + reserva.getEstado());
        }

        // Verificar si la reserva ya pasó
        if (reserva.getFecha().isBefore(LocalDate.now())) {
            throw new IllegalStateException("No se pueden cancelar reservas que ya han pasado.");
        }

        String estadoAnterior = reserva.getEstado();
        reserva.setEstado("CANCELADA");
        reservaRepository.save(reserva);

        // Registrar en historial
        registrarEnHistorial(reserva, "CANCELADA", estadoAnterior, reserva.getEstado(), "Reserva cancelada por el usuario");
    }

    /**
     * Marcar una reserva como completada
     */
    @Transactional
    public void completar(Integer idReserva) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
        if (reservaOpt.isEmpty()) {
            throw new IllegalArgumentException("La reserva con ID " + idReserva + " no existe.");
        }

        Reserva reserva = reservaOpt.get();
        String estadoAnterior = reserva.getEstado();
        reserva.setEstado("COMPLETADA");
        reservaRepository.save(reserva);

        // Registrar en historial
        registrarEnHistorial(reserva, "COMPLETADA", estadoAnterior, reserva.getEstado(), "Reserva marcada como completada");
    }

    /**
     * Verificar si existe conflicto de horario para una cancha
     */
    @Transactional(readOnly = true)
    private boolean existeConflicto(Reserva reserva) {
        List<Reserva> reservasExistentes = reservaRepository.findByCanchaAndFecha(
            reserva.getCancha().getIdCancha(),
            reserva.getFecha()
        );

        for (Reserva r : reservasExistentes) {
            // Solo considerar reservas confiramadas o pendientes
            if (r.getEstado().equals("CONFIRMADA") || r.getEstado().equals("PENDIENTE")) {
                // Verificar si hay solapamiento de horarios
                if (!(reserva.getHoraFin().isBefore(r.getHoraInicio()) || 
                      reserva.getHoraInicio().isAfter(r.getHoraFin()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Calcular el monto total de una reserva
     */
    private BigDecimal calcularMontoTotal(Reserva reserva) {
        LocalTime horaInicio = reserva.getHoraInicio();
        LocalTime horaFin = reserva.getHoraFin();

        // Calcular la diferencia en horas
        long minutos = java.time.temporal.ChronoUnit.MINUTES.between(horaInicio, horaFin);
        double horas = minutos / 60.0;

        // Multiplicar por el precio por hora
        return reserva.getCancha().getPrecioHora().multiply(BigDecimal.valueOf(horas));
    }

    /**
     * Registrar un cambio en el historial de la reserva
     */
    private void registrarEnHistorial(Reserva reserva, String accion, String estadoAnterior, 
                                      String estadoNuevo, String descripcion) {
        HistorialReserva historial = new HistorialReserva();
        historial.setReserva(reserva);
        historial.setAccion(accion);
        historial.setEstadoAnterior(estadoAnterior != null ? estadoAnterior : "NUEVA");
        historial.setEstadoNuevo(estadoNuevo);
        historial.setDescripcion(descripcion);
        historial.setUsuario(reserva.getUsuario().getEmail());
        historialReservaRepository.save(historial);
    }

    /**
     * Eliminar una reserva (solo si está en estado PENDIENTE)
     */
    @Transactional
    public void delete(Integer idReserva) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
        if (reservaOpt.isEmpty()) {
            throw new IllegalArgumentException("La reserva con ID " + idReserva + " no existe.");
        }

        Reserva reserva = reservaOpt.get();

        if (!reserva.getEstado().equals("PENDIENTE")) {
            throw new IllegalStateException("Solo se pueden eliminar reservas en estado PENDIENTE.");
        }

        reservaRepository.deleteById(idReserva);
    }
}
