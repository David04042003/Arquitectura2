package apicalendario.core.interfaces.repositorios;

import java.util.List;

import apicalendario.core.entidades.Tipo;

/**
 * Puerto de persistencia del catálogo de tipos de día.
 */
public interface ITipoRepositorio {

    List<Tipo> listar();
}
