package Proyecto_DW;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import Proyecto_DW.service.UsuarioDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    public static final String[] PUBLIC_URLS = {
        "/", "/canchas/listado", "/canchas/buscar",
        "/sobre-nosotros", "/carreras", "/blog", "/ayuda",
        "/terminos", "/privacidad", "/lista-cancha",
        "/login", "/recuperar", "/auth/registro",
        "/webjars/**", "/js/**", "/css/**", "/images/**",
        "/acceso-denegado"
    };

    public static final String[] USUARIO_URLS = {
        "/canchas/detalles/**",
        "/reserva/**", "/reservas/**",
        "/usuario/inicio", "/usuario/perfil"
    };

    public static final String[] ADMIN_URLS = {
        "/canchas/crear", "/canchas/guardar",
        "/canchas/modificar/**", "/canchas/actualizar", "/canchas/eliminar",
        "/usuario/listado", "/usuario/registro"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(requests -> requests
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(USUARIO_URLS).hasAnyRole("USUARIO", "ADMIN")
                .requestMatchers(ADMIN_URLS).hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/usuario/inicio", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"))
            .sessionManagement(session -> session
                .maximumSessions(1).maxSessionsPreventsLogin(false)
            );
        return http.build();
    }
}
