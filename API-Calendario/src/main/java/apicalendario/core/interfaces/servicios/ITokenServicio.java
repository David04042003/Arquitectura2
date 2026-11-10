package apicalendario.core.interfaces.servicios;

/**
 * Contrato para emitir y leer tokens de seguridad. Desacopla el login de la librería JWT.
 */
public interface ITokenServicio {

    String generarToken(String nombreUsuario);

    /**
     * Nombre de usuario (subject) de un token con firma válida y no expirado.
     *
     * @throws RuntimeException si el token es inválido, está mal formado o expiró
     */
    String extraerNombreUsuario(String token);
}
