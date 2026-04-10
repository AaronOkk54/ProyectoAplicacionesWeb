package Proyecto_DW.repository;

import Proyecto_DW.domain.Reserva;
import Proyecto_DW.domain.Usuario;
import Proyecto_DW.domain.Cancha;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * ReservaRepository
 * Interface para acceder a los datos de reservas en la base de datos
 */
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    /**
     * Buscar reservas por usuario
     */
    List<Reserva> findByUsuario(Usuario usuario);

    /**
     * Buscar reservas por cancha
     */
    List<Reserva> findByCancha(Cancha cancha);

    /**
     * Buscar reservas por estado
     */
    List<Reserva> findByEstado(String estado);

    /**
     * Buscar reservas de un usuario en un rango de fechas
     */
    @Query("SELECT r FROM Reserva r WHERE r.usuario.idUsuario = :idUsuario AND r.fecha >= :fechaInicio AND r.fecha <= :fechaFin")
    List<Reserva> findByUsuarioAndFechaRango(@Param("idUsuario") Integer idUsuario, 
                                              @Param("fechaInicio") LocalDate fechaInicio, 
                                              @Param("fechaFin") LocalDate fechaFin);

    /**
     * Buscar reservas de una cancha en una fecha específica
     */
    @Query("SELECT r FROM Reserva r WHERE r.cancha.idCancha = :idCancha AND r.fecha = :fecha")
    List<Reserva> findByCanchaAndFecha(@Param("idCancha") Integer idCancha, @Param("fecha") LocalDate fecha);

    /**
     * Verificar si hay reservas conflictivas para una cancha
     */
    @Query("SELECT COUNT(r) > 0 FROM Reserva r WHERE r.cancha.idCancha = :idCancha AND r.fecha = :fecha " +
           "AND r.estado IN ('PENDIENTE', 'CONFIRMADA') " +
           "AND NOT (r.horaFin <= :horaInicio OR r.horaInicio >= :horaFin)")
    boolean existeConflicto(@Param("idCancha") Integer idCancha, @Param("fecha") LocalDate fecha, 
                           @Param("horaInicio") String horaInicio, @Param("horaFin") String horaFin);

    /**
     * Listar reservas confirmadas
     */
    List<Reserva> findByEstadoOrderByFechaAsc(String estado);

    /**
     * Buscar reservas activas (no canceladas ni completadas)
     */
    @Query("SELECT r FROM Reserva r WHERE r.estado NOT IN ('CANCELADA', 'COMPLETADA') ORDER BY r.fecha ASC")
    List<Reserva> findReservasActivas();

}
