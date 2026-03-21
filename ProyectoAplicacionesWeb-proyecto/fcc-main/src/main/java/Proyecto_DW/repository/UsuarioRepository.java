package Proyecto_DW.repository;

import Proyecto_DW.domain.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * UsuarioRepository
 * Interface para acceder a los datos de usuarios en la base de datos
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Buscar usuario por email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verificar si existe un usuario con un email específico
     */
    boolean existsByEmail(String email);

    /**
     * Listar todos los usuarios activos
     */
    List<Usuario> findByActivoTrue();

    /**
     * Listar todos los usuarios inactivos
     */
    List<Usuario> findByActivoFalse();

}
