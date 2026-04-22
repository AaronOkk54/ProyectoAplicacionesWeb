/*
* Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
* Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
*/
package Proyecto_DW;



/**
*
* @author Alfaro
*/
import Proyecto_DW.service.GoogleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
@EnableWebSecurity
public class SecurityConfig {



    @Autowired
    @Lazy
    private GoogleService googleOAuth2UserService;



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {



        http.authorizeHttpRequests(requests -> requests
                // Recursos estáticos públicos
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // Endpoints OAuth2 (deben ser accesibles antes del filtro de autenticación)
                .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
                // Páginas públicas de información
                .requestMatchers("/", "/sobre-nosotros", "/carreras", "/blog",
                        "/ayuda", "/terminos", "/privacidad", "/lista-cancha").permitAll()
                // Login y registro (con y sin prefijo /auth/)
                .requestMatchers("/login", "/registro", "/auth/login", "/auth/registro", "/recuperar").permitAll()
                // Canchas: listado y detalles son públicos, gestión solo ADMIN
                .requestMatchers("/canchas/listado", "/canchas/buscar", "/canchas/detalles/**").permitAll()
                .requestMatchers("/canchas/crear", "/canchas/guardar", "/canchas/modificar/**",
                        "/canchas/actualizar").hasAnyRole("ADMIN", "VENDEDOR")
                .requestMatchers("/canchas/eliminar").hasRole("ADMIN")
                // Listado de usuarios solo ADMIN
                .requestMatchers("/admin/**", "/usuarios/**", "/usuario/listado").hasRole("ADMIN")
                // Reservas y área de usuario requieren autenticación
                .requestMatchers("/reserva/**", "/usuario/**", "/canchas/mis-canchas").hasAnyRole("ADMIN", "USUARIO", "CLIENTE", "VENDEDOR")
                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
        );



        http.formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/usuario/inicio", true)
                .failureUrl("/auth/login?error=true")
                .permitAll()

                //Inicio con google
         ).oauth2Login(oauth2 -> oauth2
                .loginPage("/auth/login")
                .defaultSuccessUrl("/usuario/inicio", true)
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(googleOAuth2UserService)
                )
        ).logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        ).exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/acceso_denegado")
        ).sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
        );



        return http.build();
    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    @Autowired
    public void configurerGlobal(AuthenticationManagerBuilder build,
            @Lazy PasswordEncoder passwordEncoder,
            @Lazy UserDetailsService userDetailsService) throws Exception {
        build.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }
}
