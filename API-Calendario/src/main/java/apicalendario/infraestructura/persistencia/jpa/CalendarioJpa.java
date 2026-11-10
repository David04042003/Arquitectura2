package apicalendario.infraestructura.persistencia.jpa;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import apicalendario.core.entidades.Calendario;

/**
 * Repositorio Spring Data JPA de la tabla calendario (detalle de infraestructura).
 */
public interface CalendarioJpa extends JpaRepository<Calendario, Integer> {

    @Query("SELECT c FROM Calendario c JOIN FETCH c.tipo WHERE c.fecha BETWEEN ?1 AND ?2 ORDER BY c.fecha")
    List<Calendario> listarEntre(LocalDate desde, LocalDate hasta);

    @Modifying
    @Query("DELETE FROM Calendario c WHERE c.fecha BETWEEN ?1 AND ?2")
    int eliminarEntre(LocalDate desde, LocalDate hasta);
}
