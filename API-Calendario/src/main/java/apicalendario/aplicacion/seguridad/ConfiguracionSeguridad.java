package apicalendario.aplicacion.seguridad;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Rutas libres de token, rutas protegidas y registro del filtro JWT.
 */
@Configuration
@EnableConfigurationProperties(PropiedadesJwt.class)
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracionSeguridad {

    private static final String[] RUTAS_LIBRES = {
            "/api/usuarios/validar/**",
            "/api/usuarios/login",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error"
    };

    private final FiltroSeguridad filtro;
    private final ObjectMapper mapeador;

    public ConfiguracionSeguridad(FiltroSeguridad filtro, ObjectMapper mapeador) {
        this.filtro = filtro;
        this.mapeador = mapeador;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // API sin sesión ni cookies: CSRF no aplica
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(RUTAS_LIBRES).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errores -> errores.authenticationEntryPoint(accesoDenegado()))
                .addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Solicitudes sin token (o con token inválido) a rutas protegidas: 403 Forbidden con un mensaje JSON.
     */
    private AuthenticationEntryPoint accesoDenegado() {
        return (request, response, excepcion) -> {
            Object motivo = request.getAttribute(FiltroSeguridad.ATRIBUTO_ERROR_TOKEN);
            String mensaje = motivo != null ? motivo.toString() : "Se requiere un token de seguridad (Bearer)";
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            mapeador.writeValue(response.getOutputStream(), Map.of("mensaje", mensaje));
        };
    }

    @Bean
    public PasswordEncoder cifradorClaves() {
        return new BCryptPasswordEncoder();
    }
}
