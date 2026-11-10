package apicalendario.infraestructura.persistencia;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import apicalendario.core.entidades.Calendario;
import apicalendario.core.interfaces.repositorios.ICalendarioRepositorio;
import apicalendario.infraestructura.persistencia.jpa.CalendarioJpa;

/**
 * Adaptador: implementa el puerto ICalendarioRepositorio del core usando Spring Data JPA.
 */
@Repository
public class CalendarioRepositorio implements ICalendarioRepositorio {

    private final CalendarioJpa jpa;

    public CalendarioRepositorio(CalendarioJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Calendario> listarPorAnio(int anio) {
        return jpa.listarEntre(LocalDate.of(anio, 1, 1), LocalDate.of(anio, 12, 31));
    }

    @Override
    @Transactional
    public List<Calendario> reemplazarAnio(int anio, List<Calendario> dias) {
        jpa.eliminarEntre(LocalDate.of(anio, 1, 1), LocalDate.of(anio, 12, 31));
        return jpa.saveAll(dias);
    }
}
