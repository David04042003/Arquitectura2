package apicalendario.infraestructura.persistencia;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import apicalendario.core.entidades.Usuario;
import apicalendario.core.interfaces.repositorios.IUsuarioRepositorio;
import apicalendario.infraestructura.persistencia.jpa.UsuarioJpa;

/**
 * Adaptador: implementa el puerto IUsuarioRepositorio del core usando Spring Data JPA.
 */
@Repository
public class UsuarioRepositorio implements IUsuarioRepositorio {

    private final UsuarioJpa jpa;

    public UsuarioRepositorio(UsuarioJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Usuario> obtenerPorUsuario(String usuario) {
        return jpa.findByUsuario(usuario);
    }

    @Override
    public boolean existe(String usuario) {
        return jpa.existsByUsuario(usuario);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return jpa.save(usuario);
    }
}
