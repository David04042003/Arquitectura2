package apicalendario.core.interfaces.repositorios;

import java.util.Optional;

import apicalendario.core.entidades.Usuario;

/**
 * Puerto de persistencia de los usuarios de la API.
 */
public interface IUsuarioRepositorio {

    Optional<Usuario> obtenerPorUsuario(String usuario);

    boolean existe(String usuario);

    Usuario guardar(Usuario usuario);
}
