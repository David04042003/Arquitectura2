package apicalendario.core.interfaces.clientes;

import java.util.List;

import apicalendario.core.dtos.FestivoDto;
import apicalendario.core.excepciones.ServicioExternoExcepcion;

/**
 * Puerto hacia la API Festivos (punto 1). La infraestructura lo implementa con un cliente HTTP.
 */
public interface IFestivoCliente {

    /**
     * Festivos de Colombia del año.
     *
     * @throws ServicioExternoExcepcion si la API Festivos no responde o responde con error
     */
    List<FestivoDto> obtenerFestivos(int anio);
}
