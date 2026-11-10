package apicalendario.core.excepciones;

/**
 * Un servicio externo del que depende el negocio (la API Festivos) no respondió correctamente.
 */
public class ServicioExternoExcepcion extends RuntimeException {

    public ServicioExternoExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
