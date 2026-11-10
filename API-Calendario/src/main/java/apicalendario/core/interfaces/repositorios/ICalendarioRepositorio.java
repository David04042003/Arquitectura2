package apicalendario.core.interfaces.repositorios;

import java.util.List;

import apicalendario.core.entidades.Calendario;

/**
 * Puerto de persistencia de los días del calendario. Lo implementa la capa de infraestructura.
 */
public interface ICalendarioRepositorio {

    /**
     * Días del año ordenados por fecha.
     */
    List<Calendario> listarPorAnio(int anio);

    /**
     * Reemplaza (en una sola transacción) los días guardados del año por los recibidos.
     * Permite volver a generar un año sin duplicar registros.
     */
    List<Calendario> reemplazarAnio(int anio, List<Calendario> dias);
}
