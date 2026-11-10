package apicalendario.core.interfaces.servicios;

import java.util.List;

import apicalendario.core.entidades.Calendario;

/**
 * Contrato de los casos de uso del calendario.
 */
public interface ICalendarioServicio {

    int ANIO_MINIMO = 1583; // primer año completo del calendario gregoriano (igual que la API Festivos)
    int ANIO_MAXIMO = 9999;

    /**
     * Clasifica cada día del año como Día laboral, Fin de Semana o Día festivo y lo guarda.
     *
     * @return true si se guardaron todos los días del año
     */
    boolean generar(int anio);

    /**
     * Días guardados del año, ordenados por fecha.
     */
    List<Calendario> listar(int anio);
}
