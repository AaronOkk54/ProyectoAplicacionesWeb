package Proyecto_DW.service;

import Proyecto_DW.domain.HistorialReserva;
import Proyecto_DW.domain.Reserva;
import Proyecto_DW.repository.HistorialReservaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HistorialReservaService
 * Servicio para la gestión del historial y auditoría de reservas
 */
@Service
public class HistorialReservaService {

    private final HistorialReservaRepository historialReservaRepository;

    public HistorialReservaService(HistorialReservaRepository historialReservaRepository) {
        this.historialReservaRepository = historialReservaRepository;
    }

    /**
     * Obtener todos los registros del historial
     */
    @Transactional(readOnly = true)
    public List<HistorialReserva> getHistorial() {
        return historialReservaRepository.findAll();
    }

    /**
     * Obtener un registro del historial por ID
     */
    @Transactional(readOnly = true)
    public Optional<HistorialReserva> getHistorialPorId(Integer idHistorial) {
        return historialReservaRepository.findById(idHistorial);
    }

    /**
     * Obtener historial completo de una reserva
     */
    @Transactional(readOnly = true)
    public List<HistorialReserva> getHistorialPorReserva(Reserva reserva) {
        return historialReservaRepository.findByReservaOrderByFechaCambioDesc(reserva);
    }

    /**
     * Obtener historial de una reserva ordenado por fecha (más reciente primero)
     */
    @Transactional(readOnly = true)
    public List<HistorialReserva> getHistorialPorReservaOrdenado(Reserva reserva) {
        return historialReservaRepository.findByReservaOrderByFechaCambioDesc(reserva);
    }

    /**
     * Obtener cambios por acción específica
     */
    @Transactional(readOnly = true)
    public List<HistorialReserva> getHistorialPorAccion(String accion) {
        return historialReservaRepository.findByAccion(accion);
    }

    /**
     * Obtener cambios realizados por un usuario
     */
    @Transactional(readOnly = true)
    public List<HistorialReserva> getHistorialPorUsuario(String usuario) {
        return historialReservaRepository.findByUsuario(usuario);
    }

    /**
     * Obtener cambios en un rango de fechas
     */
    @Transactional(readOnly = true)
    public List<HistorialReserva> getHistorialPorFechaRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return historialReservaRepository.findByFechaRango(fechaInicio, fechaFin);
    }

    /**
     * Guardar un nuevo registro en el historial
     */
    @Transactional
    public HistorialReserva save(HistorialReserva historial) {
        if (historial.getReserva() == null) {
            throw new IllegalArgumentException("La reserva es requerida.");
        }

        if (historial.getAccion() == null || historial.getAccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La acción no puede estar vacía.");
        }

        if (historial.getEstadoNuevo() == null || historial.getEstadoNuevo().trim().isEmpty()) {
            throw new IllegalArgumentException("El estado nuevo no puede estar vacío.");
        }

        return historialReservaRepository.save(historial);
    }

    /**
     * Contar cambios para una reserva (auditoría)
     */
    @Transactional(readOnly = true)
    public long contarCambiosPorReserva(Integer idReserva) {
        return historialReservaRepository.countByReserva(idReserva);
    }

    /**
     * Obtener el último cambio de una reserva
     */
    @Transactional(readOnly = true)
    public Optional<HistorialReserva> getUltimoCambio(Reserva reserva) {
        List<HistorialReserva> historial = historialReservaRepository.findByReservaOrderByFechaCambioDesc(reserva);
        if (historial.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(historial.get(0));
    }

    /**
     * Eliminar registro del historial (solo administrador)
     */
    @Transactional
    public void delete(Integer idHistorial) {
        if (!historialReservaRepository.existsById(idHistorial)) {
            throw new IllegalArgumentException("El registro del historial con ID " + idHistorial + " no existe.");
        }
        historialReservaRepository.deleteById(idHistorial);
    }

    /**
     * Eliminar todo el historial de una reserva (solo administrador)
     */
    @Transactional
    public void deleteHistorialPorReserva(Reserva reserva) {
        List<HistorialReserva> historiales = historialReservaRepository.findByReserva(reserva);
        historialReservaRepository.deleteAll(historiales);
    }
}
