package apicalendario.aplicacion.seguridad;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import apicalendario.core.entidades.Usuario;

/**
 * Adapta la entidad Usuario al contrato UserDetails de Spring Security.
 */
public class UsuarioDetalles implements UserDetails {

    private final String nombreUsuario;
    private final String clave;
    private final List<GrantedAuthority> permisos;

    public UsuarioDetalles(Usuario usuario) {
        this.nombreUsuario = usuario.getUsuario();
        this.clave = usuario.getClave();
        this.permisos = usuario.getRoles() == null || usuario.getRoles().isBlank()
                ? List.of()
                : Arrays.stream(usuario.getRoles().split(","))
                        .map(String::trim)
                        .filter(rol -> !rol.isEmpty())
                        .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                        .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permisos;
    }

    @Override
    public String getPassword() {
        return clave;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }
}
