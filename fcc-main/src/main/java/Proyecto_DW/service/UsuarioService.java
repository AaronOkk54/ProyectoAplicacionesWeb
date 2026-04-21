package Proyecto_DW.service;

import Proyecto_DW.domain.Usuario;
import Proyecto_DW.domain.Rol;
import Proyecto_DW.repository.UsuarioRepository;
import Proyecto_DW.repository.RolRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
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

        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);
        usuario.setFechaCreacion(java.time.LocalDateTime.now());

        return usuarioRepository.save(asignarRol(usuario, "CLIENTE"));
    }

    @Transactional
    public Usuario registrar(Usuario usuario, String rolNombre) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado en el sistema.");
        }
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del usuario no puede estar vacío.");
        }
        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);
        usuario.setFechaCreacion(java.time.LocalDateTime.now());
        return usuarioRepository.save(asignarRol(usuario, rolNombre));
    }

    private Usuario asignarRol(Usuario usuario, String rolNombre) {
        Rol rol = rolRepository.findByNombre(rolNombre);
        if (rol == null) rol = rolRepository.findByNombre("CLIENTE");
        if (rol == null) rol = rolRepository.findByNombre("USUARIO");
        if (rol != null) {
            Set<Rol> roles = new HashSet<>();
            roles.add(rol);
            usuario.setRoles(roles);
        }
        return usuario;
    }

    @Transactional
    public Usuario save(Usuario usuario) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
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

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
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

        return passwordEncoder.matches(password, usuario.getPassword());
    }

    @Transactional
    public void delete(Integer idUsuario) {

        if (!usuarioRepository.existsById(idUsuario)) {
            throw new IllegalArgumentException("El usuario con ID " + idUsuario + " no existe.");
        }

        usuarioRepository.deleteById(idUsuario);
    }
}