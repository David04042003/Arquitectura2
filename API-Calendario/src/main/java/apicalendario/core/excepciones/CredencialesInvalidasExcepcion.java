package apicalendario.core.excepciones;

/**
 * Usuario o clave incorrectos.
 */
public class CredencialesInvalidasExcepcion extends RuntimeException {

    public CredencialesInvalidasExcepcion() {
        super("Usuario o clave incorrectos");
    }
}
