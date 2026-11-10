package apicalendario.core.interfaces.servicios;

import apicalendario.core.dtos.UsuarioLoginDto;
import apicalendario.core.excepciones.CredencialesInvalidasExcepcion;

/**
 * Contrato del caso de uso de autenticación.
 */
public interface IUsuarioServicio {

    /**
     * Valida las credenciales y emite un token de seguridad.
     *
     * @throws CredencialesInvalidasExcepcion si el usuario no existe o la clave no coincide
     */
    UsuarioLoginDto login(String usuario, String clave);
}
