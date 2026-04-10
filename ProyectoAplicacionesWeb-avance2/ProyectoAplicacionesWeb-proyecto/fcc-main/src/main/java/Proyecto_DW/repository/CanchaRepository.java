package Proyecto_DW.repository;

import Proyecto_DW.domain.Cancha;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * CanchaRepository
 * Interface para acceder a los datos de canchas en la base de datos
 */
public interface CanchaRepository extends JpaRepository<Cancha, Integer> {

    /**
     * Buscar canchas por nombre
     */
    List<Cancha> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Buscar canchas disponibles (estado = DISPONIBLE)
     */
    List<Cancha> findByEstado(String estado);

    /**
     * Buscar canchas disponibles ordenadas por precio
     */
    @Query("SELECT c FROM Cancha c WHERE c.estado = 'DISPONIBLE' ORDER BY c.precioHora ASC")
    List<Cancha> findCanchasDisponibles();

    /**
     * Buscar por ubicación
     */
    List<Cancha> findByUbicacionContainingIgnoreCase(String ubicacion);

}
