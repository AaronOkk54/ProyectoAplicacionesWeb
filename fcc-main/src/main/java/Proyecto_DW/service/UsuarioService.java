package Proyecto_DW.service;

import Proyecto_DW.domain.Usuario;
import Proyecto_DW.domain.Rol;
import Proyecto_DW.repository.UsuarioRepository;
import Proyecto_DW.repository.RolRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UsuarioService
 * Servicio para la gestión de usuarios del sistema
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuariosActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Transactional
    public Usuario registrar(Usuario usuario) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado en el sistema.");
        }

        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del usuario no puede estar vacío.");
        }

        // guardar contraseña normal (sin encriptar por ahora)
        usuario.setPassword(usuario.getPassword());
        usuario.setActivo(true);

        Rol rolUsuario = rolRepository.findByNombre("USUARIO");

        if (rolUsuario != null) {
            Set<Rol> roles = new HashSet<>();
            roles.add(rolUsuario);
            usuario.setRoles(roles);
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario save(Usuario usuario) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }

        usuario.setPassword(usuario.getPassword());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario update(Usuario usuario) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuario.getIdUsuario());

        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("El usuario con ID " + usuario.getIdUsuario() + " no existe.");
        }

        Usuario usuarioExistente = usuarioOpt.get();
        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setTelefono(usuario.getTelefono());

        return usuarioRepository.save(usuarioExistente);
    }

    @Transactional
    public void cambiarPassword(Integer idUsuario, String passwordActual, String passwordNueva) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);

        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("El usuario no existe.");
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.getPassword().equals(passwordActual)) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        usuario.setPassword(passwordNueva);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void activar(Integer idUsuario) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);

        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("El usuario con ID " + idUsuario + " no existe.");
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setActivo(true);

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desactivar(Integer idUsuario) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);

        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("El usuario con ID " + idUsuario + " no existe.");
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setActivo(false);

        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public boolean verificarCredenciales(String email, String password) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.isActivo()) {
            return false;
        }

        return usuario.getPassword().equals(password);
    }

    @Transactional
    public void delete(Integer idUsuario) {

        if (!usuarioRepository.existsById(idUsuario)) {
            throw new IllegalArgumentException("El usuario con ID " + idUsuario + " no existe.");
        }

        usuarioRepository.deleteById(idUsuario);
    }
}