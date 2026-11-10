package apicalendario.aplicacion;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import apicalendario.core.dtos.FestivoDto;
import apicalendario.core.entidades.Calendario;
import apicalendario.core.entidades.Tipo;
import apicalendario.core.excepciones.SolicitudInvalidaExcepcion;
import apicalendario.core.interfaces.clientes.IFestivoCliente;
import apicalendario.core.interfaces.repositorios.ICalendarioRepositorio;
import apicalendario.core.interfaces.repositorios.ITipoRepositorio;
import apicalendario.core.interfaces.servicios.ICalendarioServicio;

/**
 * Casos de uso del calendario. Solo conoce interfaces del core (repositorios y cliente de festivos);
 * Spring inyecta las implementaciones de la infraestructura por constructor.
 */
@Service
public class CalendarioServicio implements ICalendarioServicio {

    private static final Locale ESPANOL = Locale.forLanguageTag("es-CO");

    private final ICalendarioRepositorio calendarioRepositorio;
    private final ITipoRepositorio tipoRepositorio;
    private final IFestivoCliente festivoCliente;

    public CalendarioServicio(ICalendarioRepositorio calendarioRepositorio,
                              ITipoRepositorio tipoRepositorio,
                              IFestivoCliente festivoCliente) {
        this.calendarioRepositorio = calendarioRepositorio;
        this.tipoRepositorio = tipoRepositorio;
        this.festivoCliente = festivoCliente;
    }

    @Override
    public boolean generar(int anio) {
        validarAnio(anio);

        // Primero se consulta todo lo externo: si la API Festivos falla no se toca lo ya guardado.
        Map<LocalDate, String> festivos = festivosPorFecha(festivoCliente.obtenerFestivos(anio));
        Map<Integer, Tipo> tipos = tiposPorId();

        LocalDate inicio = LocalDate.of(anio, 1, 1);
        List<Calendario> dias = inicio.datesUntil(inicio.plusYears(1))
                .map(fecha -> clasificar(fecha, festivos, tipos))
                .toList();

        List<Calendario> guardados = calendarioRepositorio.reemplazarAnio(anio, dias);
        return guardados.size() == dias.size();
    }

    @Override
    public List<Calendario> listar(int anio) {
        validarAnio(anio);
        return calendarioRepositorio.listarPorAnio(anio);
    }

    /**
     * Regla de negocio: un festivo prevalece sobre el fin de semana; sábado y domingo son fin de semana;
     * el resto son días laborales. La descripción es el nombre del festivo o el nombre del día.
     */
    private Calendario clasificar(LocalDate fecha, Map<LocalDate, String> festivos, Map<Integer, Tipo> tipos) {
        String festivo = festivos.get(fecha);
        if (festivo != null) {
            return new Calendario(fecha, tipos.get(Tipo.DIA_FESTIVO), festivo);
        }
        String nombreDia = nombreDia(fecha.getDayOfWeek());
        if (esFinDeSemana(fecha.getDayOfWeek())) {
            return new Calendario(fecha, tipos.get(Tipo.FIN_DE_SEMANA), nombreDia);
        }
        return new Calendario(fecha, tipos.get(Tipo.DIA_LABORAL), nombreDia);
    }

    private static boolean esFinDeSemana(DayOfWeek dia) {
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
    }

    private static String nombreDia(DayOfWeek dia) {
        String nombre = dia.getDisplayName(TextStyle.FULL, ESPANOL);
        return nombre.substring(0, 1).toUpperCase(ESPANOL) + nombre.substring(1);
    }

    /**
     * Si dos festivos caen el mismo día se unen sus nombres.
     */
    private static Map<LocalDate, String> festivosPorFecha(List<FestivoDto> festivos) {
        return festivos.stream()
                .collect(Collectors.toMap(FestivoDto::fecha, FestivoDto::festivo, (a, b) -> a + " / " + b));
    }

    private Map<Integer, Tipo> tiposPorId() {
        Map<Integer, Tipo> tipos = tipoRepositorio.listar().stream()
                .collect(Collectors.toMap(Tipo::getId, Function.identity()));
        for (int id : new int[] { Tipo.DIA_LABORAL, Tipo.FIN_DE_SEMANA, Tipo.DIA_FESTIVO }) {
            if (!tipos.containsKey(id)) {
                throw new IllegalStateException("Falta el tipo de día con id " + id + " en la tabla tipo");
            }
        }
        return tipos;
    }

    private static void validarAnio(int anio) {
        if (anio < ANIO_MINIMO || anio > ANIO_MAXIMO) {
            throw new SolicitudInvalidaExcepcion(
                    "Año no válido. Debe ser un entero entre " + ANIO_MINIMO + " y " + ANIO_MAXIMO);
        }
    }
}
