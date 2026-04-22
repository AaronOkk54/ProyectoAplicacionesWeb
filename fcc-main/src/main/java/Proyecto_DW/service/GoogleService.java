package Proyecto_DW.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import Proyecto_DW.domain.Rol;
import Proyecto_DW.domain.Usuario;
import Proyecto_DW.repository.RolRepository;
import Proyecto_DW.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;

@Service
public class GoogleService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final HttpSession session;

    public GoogleService(UsuarioRepository usuarioRepository,
                         RolRepository rolRepository,
                         HttpSession session) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.session = session;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String nombre = oAuth2User.getAttribute("name");

        // Buscar usuario en la BD o crearlo si no existe
        Usuario usuario = usuarioRepository.findByEmail(email).orElseGet(() -> {
            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre);
            nuevo.setEmail(email);
            nuevo.setPassword("");
            nuevo.setActivo(true);
            nuevo.setFechaCreacion(LocalDateTime.now());

            // Asignar rol CLIENTE por defecto
            Rol rol = rolRepository.findByNombre("CLIENTE");
            if (rol == null) rol = rolRepository.findByNombre("USUARIO");
            if (rol != null) {
                nuevo.setRoles(new HashSet<>(Set.of(rol)));
            } else {
                nuevo.setRoles(new HashSet<>());
            }

            return usuarioRepository.save(nuevo);
        });

        // Guardar nombre en sesión
        session.removeAttribute("nombreUsuario");
        session.setAttribute("nombreUsuario", usuario.getNombre());

        // Mapear roles al formato de Spring Security
        Set<SimpleGrantedAuthority> authorities;
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            authorities = Set.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        } else {
            authorities = usuario.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getNombre()))
                    .collect(Collectors.toSet());
        }

        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "email");
    }
}
