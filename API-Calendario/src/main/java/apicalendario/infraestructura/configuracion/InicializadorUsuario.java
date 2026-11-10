package apicalendario.infraestructura.configuracion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import apicalendario.core.entidades.Usuario;
import apicalendario.core.interfaces.repositorios.IUsuarioRepositorio;

/**
 * Crea el usuario inicial (con la clave cifrada en BCrypt) para poder obtener el primer token.
 */
@Component
public class InicializadorUsuario implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InicializadorUsuario.class);

    private final IUsuarioRepositorio repositorio;
    private final PasswordEncoder cifrador;
    private final PropiedadesUsuarioInicial propiedades;

    public InicializadorUsuario(IUsuarioRepositorio repositorio, PasswordEncoder cifrador,
                                PropiedadesUsuarioInicial propiedades) {
        this.repositorio = repositorio;
        this.cifrador = cifrador;
        this.propiedades = propiedades;
    }

    @Override
    public void run(String... args) {
        if (propiedades.usuario() == null || propiedades.usuario().isBlank()
                || repositorio.existe(propiedades.usuario())) {
            return;
        }
        repositorio.guardar(new Usuario(
                propiedades.usuario(),
                propiedades.nombre(),
                cifrador.encode(propiedades.clave()),
                propiedades.roles()));
        LOG.info("Usuario inicial '{}' creado", propiedades.usuario());
    }
}
