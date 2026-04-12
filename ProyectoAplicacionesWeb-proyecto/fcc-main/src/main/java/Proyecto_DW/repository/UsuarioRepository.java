package Proyecto_DW.repository;

import Proyecto_DW.domain.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmailAndActivoTrue(String email);
    
    List<Usuario> findByActivoTrue();
    
    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findByEmailAndPassword(String email, String password);
    
    boolean existsByEmail(String email);
}
