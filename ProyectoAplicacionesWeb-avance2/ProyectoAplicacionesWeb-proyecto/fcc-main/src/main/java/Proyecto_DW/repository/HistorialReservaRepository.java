package Proyecto_DW.repository;

import Proyecto_DW.domain.HistorialReserva;
import Proyecto_DW.domain.Reserva;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * HistorialReservaRepository
 * Interface para acceder a los datos del historial de reservas en la base de datos
 */
public interface HistorialReservaRepository extends JpaRepository<HistorialReserva, Integer> {

    /**
     * Buscar historial por reserva
     */
    List<HistorialReserva> findByReserva(Reserva reserva);

    /**
     * Buscar historial ordenado por fecha (más reciente primero)
     */
    List<HistorialReserva> findByReservaOrderByFechaCambioDesc(Reserva reserva);

    /**
     * Buscar cambios por acción
     */
    List<HistorialReserva> findByAccion(String accion);

    /**
     * Buscar cambios realizados por un usuario
     */
    List<HistorialReserva> findByUsuario(String usuario);

    /**
     * Buscar cambios en un rango de fechas
     */
    @Query("SELECT h FROM HistorialReserva h WHERE h.fechaCambio >= :fechaInicio AND h.fechaCambio <= :fechaFin ORDER BY h.fechaCambio DESC")
    List<HistorialReserva> findByFechaRango(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                             @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Contar cambios para auditoría
     */
    @Query("SELECT COUNT(h) FROM HistorialReserva h WHERE h.reserva.idReserva = :idReserva")
    long countByReserva(@Param("idReserva") Integer idReserva);

}
