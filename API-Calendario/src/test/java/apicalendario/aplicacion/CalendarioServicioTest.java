package apicalendario.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import apicalendario.core.dtos.FestivoDto;
import apicalendario.core.entidades.Calendario;
import apicalendario.core.entidades.Tipo;
import apicalendario.core.excepciones.ServicioExternoExcepcion;
import apicalendario.core.excepciones.SolicitudInvalidaExcepcion;
import apicalendario.core.interfaces.clientes.IFestivoCliente;
import apicalendario.core.interfaces.repositorios.ICalendarioRepositorio;
import apicalendario.core.interfaces.repositorios.ITipoRepositorio;

/**
 * Pruebas de la lógica de clasificación sin base de datos ni API Festivos:
 * gracias a la arquitectura Onion basta con implementaciones en memoria de las interfaces del core.
 */
class CalendarioServicioTest {

    private RepositorioEnMemoria repositorio;
    private List<FestivoDto> festivos;
    private boolean apiFestivosCaida;
    private CalendarioServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioEnMemoria();
        festivos = new ArrayList<>(List.of(
                new FestivoDto("Año nuevo", LocalDate.of(2023, 1, 1)),
                new FestivoDto("Santos Reyes", LocalDate.of(2023, 1, 9)),
                new FestivoDto("Navidad", LocalDate.of(2023, 12, 25))));
        apiFestivosCaida = false;

        ITipoRepositorio tipos = () -> List.of(
                new Tipo(Tipo.DIA_LABORAL, "Día laboral"),
                new Tipo(Tipo.FIN_DE_SEMANA, "Fin de Semana"),
                new Tipo(Tipo.DIA_FESTIVO, "Día festivo"));
        IFestivoCliente cliente = anio -> {
            if (apiFestivosCaida) {
                throw new ServicioExternoExcepcion("API Festivos no disponible", null);
            }
            return festivos;
        };
        servicio = new CalendarioServicio(repositorio, tipos, cliente);
    }

    @Test
    void generaTodosLosDiasDelAnio() {
        assertTrue(servicio.generar(2023));
        assertEquals(365, repositorio.listarPorAnio(2023).size());

        assertTrue(servicio.generar(2024));
        assertEquals(366, repositorio.listarPorAnio(2024).size(), "2024 es bisiesto");
    }

    @Test
    void clasificaFestivosFinesDeSemanaYLaborales() {
        servicio.generar(2023);

        // 1 de enero de 2023 es domingo y festivo: prevalece el festivo
        assertDia("2023-01-01", Tipo.DIA_FESTIVO, "Año nuevo");
        // 9 de enero de 2023 es lunes festivo
        assertDia("2023-01-09", Tipo.DIA_FESTIVO, "Santos Reyes");
        assertDia("2023-01-07", Tipo.FIN_DE_SEMANA, "Sábado");
        assertDia("2023-01-08", Tipo.FIN_DE_SEMANA, "Domingo");
        assertDia("2023-01-02", Tipo.DIA_LABORAL, "Lunes");
        assertDia("2023-01-04", Tipo.DIA_LABORAL, "Miércoles");
    }

    @Test
    void volverAGenerarNoDuplicaDias() {
        servicio.generar(2023);
        servicio.generar(2023);
        assertEquals(365, repositorio.dias.size());
    }

    @Test
    void siLaApiFestivosFallaNoSeModificaLoGuardado() {
        servicio.generar(2023);
        apiFestivosCaida = true;

        assertThrows(ServicioExternoExcepcion.class, () -> servicio.generar(2023));
        assertEquals(365, repositorio.dias.size());
    }

    @Test
    void rechazaAniosFueraDeRango() {
        assertThrows(SolicitudInvalidaExcepcion.class, () -> servicio.generar(1500));
        assertThrows(SolicitudInvalidaExcepcion.class, () -> servicio.listar(10000));
    }

    @Test
    void listaSoloLosDiasDelAnioPedido() {
        servicio.generar(2023);
        servicio.generar(2024);

        List<Calendario> dias = servicio.listar(2023);
        assertEquals(365, dias.size());
        assertEquals(LocalDate.of(2023, 1, 1), dias.get(0).getFecha());
        assertEquals(LocalDate.of(2023, 12, 31), dias.get(364).getFecha());
    }

    private void assertDia(String fecha, int idTipo, String descripcion) {
        Calendario dia = repositorio.dias.stream()
                .filter(d -> d.getFecha().equals(LocalDate.parse(fecha)))
                .findFirst()
                .orElseThrow();
        assertEquals(idTipo, dia.getTipo().getId(), "tipo de " + fecha);
        assertEquals(descripcion, dia.getDescripcion(), "descripción de " + fecha);
    }

    /** Implementación en memoria del puerto de persistencia. */
    private static class RepositorioEnMemoria implements ICalendarioRepositorio {

        private final List<Calendario> dias = new ArrayList<>();

        @Override
        public List<Calendario> listarPorAnio(int anio) {
            return dias.stream().filter(d -> d.getFecha().getYear() == anio).toList();
        }

        @Override
        public List<Calendario> reemplazarAnio(int anio, List<Calendario> nuevos) {
            dias.removeIf(d -> d.getFecha().getYear() == anio);
            dias.addAll(nuevos);
            return nuevos;
        }
    }
}
