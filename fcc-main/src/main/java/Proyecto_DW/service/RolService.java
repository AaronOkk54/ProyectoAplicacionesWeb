package Proyecto_DW.service;

import Proyecto_DW.domain.Rol;
import Proyecto_DW.repository.RolRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RolService
 * Servicio para la gestión de roles en el sistema
 */
@Service
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    /**
     * Obtener todos los roles
     */
    @Transactional(readOnly = true)
    public List<Rol> getRoles() {
        return rolRepository.findAll();
    }

    /**
     * Obtener un rol por ID
     */
    @Transactional(readOnly = true)
    public Optional<Rol> getRol(Integer idRol) {
        return rolRepository.findById(idRol);
    }

    /**
     * Obtener rol por nombre
     */
    @Transactional(readOnly = true)
    public Rol getRolByNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }

    /**
     * Guardar un nuevo rol
     */
    @Transactional
    public Rol save(Rol rol) {
        if (rol.getNombre() == null || rol.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del rol no puede estar vacío.");
        }
        return rolRepository.save(rol);
    }

    /**
     * Actualizar un rol existente
     */
    @Transactional
    public Rol update(Rol rol) {
        if (!rolRepository.existsById(rol.getIdRol())) {
            throw new IllegalArgumentException("El rol con ID " + rol.getIdRol() + " no existe.");
        }
        return rolRepository.save(rol);
    }

    /**
     * Eliminar un rol
     */
    @Transactional
    public void delete(Integer idRol) {
        if (!rolRepository.existsById(idRol)) {
            throw new IllegalArgumentException("El rol con ID " + idRol + " no existe.");
        }
        rolRepository.deleteById(idRol);
    }
}
