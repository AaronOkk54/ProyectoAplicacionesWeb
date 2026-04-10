package Proyecto_DW.service;

import Proyecto_DW.domain.Rol;
import Proyecto_DW.domain.Usuario;
import Proyecto_DW.repository.UsuarioRepository;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        Set<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
            .map(Rol::getNombre)
            .map(nombre -> new SimpleGrantedAuthority("ROLE_" + nombre))
            .collect(Collectors.toSet());

        return new User(usuario.getEmail(), usuario.getPassword(), authorities);
    }
}
