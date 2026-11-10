package apicalendario.core.excepciones;

/**
 * Los datos de entrada no cumplen las reglas del negocio (por ejemplo, un año fuera de rango).
 */
public class SolicitudInvalidaExcepcion extends RuntimeException {

    public SolicitudInvalidaExcepcion(String mensaje) {
        super(mensaje);
    }
}
