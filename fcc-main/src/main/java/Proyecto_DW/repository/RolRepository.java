package Proyecto_DW.repository;

import Proyecto_DW.domain.Rol;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * RolRepository
 * Interface para acceder a los datos de roles en la base de datos
 */
public interface RolRepository extends JpaRepository<Rol, Integer> {

    /**
     * Buscar un rol por su nombre
     */
    Rol findByNombre(String nombre);

}
