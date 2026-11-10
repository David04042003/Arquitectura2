package apicalendario.infraestructura.persistencia;

import java.util.List;

import org.springframework.stereotype.Repository;

import apicalendario.core.entidades.Tipo;
import apicalendario.core.interfaces.repositorios.ITipoRepositorio;
import apicalendario.infraestructura.persistencia.jpa.TipoJpa;

/**
 * Adaptador: implementa el puerto ITipoRepositorio del core usando Spring Data JPA.
 */
@Repository
public class TipoRepositorio implements ITipoRepositorio {

    private final TipoJpa jpa;

    public TipoRepositorio(TipoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Tipo> listar() {
        return jpa.findAll();
    }
}
