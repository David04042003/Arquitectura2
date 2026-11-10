package apicalendario.aplicacion.seguridad;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import apicalendario.core.interfaces.servicios.ITokenServicio;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lee el header "Authorization: Bearer {token}" de cada solicitud y, si el token es válido,
 * autentica al usuario en el contexto de Spring Security.
 * Si no hay token o es inválido la solicitud sigue sin autenticar y la configuración responde 403.
 */
@Component
public class FiltroSeguridad extends OncePerRequestFilter {

    /** Atributo de la solicitud con el motivo por el que se rechazó el token. */
    public static final String ATRIBUTO_ERROR_TOKEN = "apicalendario.errorToken";

    private static final String PREFIJO = "Bearer ";

    private final ITokenServicio servicioToken;
    private final UsuarioDetalleServicio servicioUsuario;

    public FiltroSeguridad(ITokenServicio servicioToken, UsuarioDetalleServicio servicioUsuario) {
        this.servicioToken = servicioToken;
        this.servicioUsuario = servicioUsuario;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (encabezado != null && encabezado.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = encabezado.substring(PREFIJO.length()).trim();
            try {
                String nombreUsuario = servicioToken.extraerNombreUsuario(token);
                UserDetails usuario = servicioUsuario.loadUserByUsername(nombreUsuario);

                UsernamePasswordAuthenticationToken autenticacion =
                        new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (ExpiredJwtException e) {
                request.setAttribute(ATRIBUTO_ERROR_TOKEN, "El token expiró");
            } catch (JwtException | IllegalArgumentException e) {
                request.setAttribute(ATRIBUTO_ERROR_TOKEN, "El token no es válido");
            } catch (UsernameNotFoundException e) {
                request.setAttribute(ATRIBUTO_ERROR_TOKEN, "El usuario del token no existe");
            }
        }

        filterChain.doFilter(request, response);
    }
}
