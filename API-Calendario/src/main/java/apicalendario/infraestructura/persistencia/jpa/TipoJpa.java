package apicalendario.infraestructura.persistencia.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import apicalendario.core.entidades.Tipo;

/**
 * Repositorio Spring Data JPA de la tabla tipo.
 */
public interface TipoJpa extends JpaRepository<Tipo, Integer> {
}
