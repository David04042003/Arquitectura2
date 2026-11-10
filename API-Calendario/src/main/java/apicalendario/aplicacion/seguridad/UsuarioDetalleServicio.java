package apicalendario.aplicacion.seguridad;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import apicalendario.core.interfaces.repositorios.IUsuarioRepositorio;

/**
 * Carga los usuarios desde la base de datos (a través del puerto IUsuarioRepositorio) para Spring Security.
 */
@Service
public class UsuarioDetalleServicio implements UserDetailsService {

    private final IUsuarioRepositorio repositorio;

    public UsuarioDetalleServicio(IUsuarioRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        return repositorio.obtenerPorUsuario(nombreUsuario)
                .map(UsuarioDetalles::new)
                .orElseThrow(() -> new UsernameNotFoundException(nombreUsuario));
    }
}
