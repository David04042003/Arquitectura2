package apicalendario.aplicacion;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import apicalendario.core.dtos.UsuarioDto;
import apicalendario.core.dtos.UsuarioLoginDto;
import apicalendario.core.entidades.Usuario;
import apicalendario.core.excepciones.CredencialesInvalidasExcepcion;
import apicalendario.core.interfaces.repositorios.IUsuarioRepositorio;
import apicalendario.core.interfaces.servicios.ITokenServicio;
import apicalendario.core.interfaces.servicios.IUsuarioServicio;

/**
 * Caso de uso de login: valida las credenciales y entrega el usuario junto con su token.
 */
@Service
public class UsuarioServicio implements IUsuarioServicio {

    private final IUsuarioRepositorio repositorio;
    private final ITokenServicio servicioToken;
    private final PasswordEncoder cifrador;

    public UsuarioServicio(IUsuarioRepositorio repositorio, ITokenServicio servicioToken, PasswordEncoder cifrador) {
        this.repositorio = repositorio;
        this.servicioToken = servicioToken;
        this.cifrador = cifrador;
    }

    @Override
    public UsuarioLoginDto login(String nombreUsuario, String clave) {
        Usuario usuario = repositorio.obtenerPorUsuario(nombreUsuario)
                .filter(u -> cifrador.matches(clave, u.getClave()))
                .orElseThrow(CredencialesInvalidasExcepcion::new);

        return new UsuarioLoginDto(UsuarioDto.desde(usuario), servicioToken.generarToken(usuario.getUsuario()));
    }
}
