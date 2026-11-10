package apicalendario.aplicacion.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración del token JWT (prefijo "seguridad.jwt").
 *
 * @param secreto           clave HMAC en Base64 de al menos 256 bits
 * @param expiracionMinutos vigencia del token
 */
@ConfigurationProperties(prefix = "seguridad.jwt")
public record PropiedadesJwt(String secreto, @DefaultValue("30") long expiracionMinutos) {
}
