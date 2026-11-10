package apicalendario.infraestructura.persistencia.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import apicalendario.core.entidades.Usuario;

/**
 * Repositorio Spring Data JPA de la tabla usuario.
 */
public interface UsuarioJpa extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);
}
