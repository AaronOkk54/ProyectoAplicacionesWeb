package Proyecto_DW.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(GoogleService.class);

    public GoogleService(UsuarioRepository usuarioRepository,
                         RolRepository rolRepository,
                         HttpSession session) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.session = session;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        var delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String nombre = oAuth2User.getAttribute("name");

        // Obtener la clave usada por el proveedor para identificar el nombre (por ejemplo: "sub" o "email")
        String nameAttributeKey = userRequest.getClientRegistration()
            .getProviderDetails()
            .getUserInfoEndpoint()
            .getUserNameAttributeName();

        // Logging de atributos recibidos (útil para depuración)
        try {
            logger.debug("Google OAuth2 attributes: {}", oAuth2User.getAttributes());
        } catch (Exception ex) {
            // no bloquear
        }

        // Si no viene email, intentar construir uno a partir del 'sub' (id de Google)
        boolean syntheticEmail = false;
        if (email == null || email.isBlank()) {
            Object sub = oAuth2User.getAttribute("sub");
            if (sub != null) {
                email = sub.toString() + "@google.local";
                syntheticEmail = true;
                logger.warn("Google returned no email; using synthetic email: {}", email);
            } else {
                logger.warn("Google OAuth2 returned no email nor sub; user won't be persisted");
            }
        }

        // Buscar usuario en la BD o crearlo si no existe (requiere email)
        Usuario usuario = null;
        if (email != null) {
            var found = usuarioRepository.findByEmail(email);
            if (found.isPresent()) {
                usuario = found.get();
            } else {
                Usuario nuevo = new Usuario();
                nuevo.setNombre(nombre != null && !nombre.isBlank() ? nombre : (email));
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

                try {
                    usuario = usuarioRepository.save(nuevo);
                    logger.info("Usuario creado a partir de Google: id={}, email={}, syntheticEmail={}", usuario.getIdUsuario(), usuario.getEmail(), syntheticEmail);
                } catch (Exception ex) {
                    logger.error("Error al guardar usuario desde Google OAuth2: {}", ex.getMessage(), ex);
                    // No interrumpir el flujo: mantener usuario nulo
                }
            }
        }

        // Si el usuario no pudo ser guardado, lanzar error apropiado
        if (usuario == null) {
            logger.error("No se pudo obtener o crear usuario para OAuth2 con email={}", email);
            throw new org.springframework.security.oauth2.core.OAuth2AuthenticationException(
                new org.springframework.security.oauth2.core.OAuth2Error("user_creation_failed"),
                "No se pudo crear el usuario. Por favor intente de nuevo.");
        }

        // Guardar nombre en sesión (fallback a email si nombre es nulo)
        String displayName = usuario.getNombre() != null && !usuario.getNombre().isBlank()
            ? usuario.getNombre()
            : (email != null ? email : String.valueOf(usuario.getIdUsuario()));
        session.setAttribute("nombreUsuario", displayName);

        // Mapear roles al formato de Spring Security
        Set<SimpleGrantedAuthority> authorities;
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            authorities = Set.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        } else {
            authorities = usuario.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getNombre()))
                    .collect(Collectors.toSet());
        }

        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), nameAttributeKey);
    }
}
