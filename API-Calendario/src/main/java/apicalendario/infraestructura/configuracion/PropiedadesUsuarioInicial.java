package apicalendario.infraestructura.configuracion;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Usuario que se crea al iniciar si la tabla usuario no lo tiene (prefijo "seguridad.usuario-inicial").
 */
@ConfigurationProperties(prefix = "seguridad.usuario-inicial")
public record PropiedadesUsuarioInicial(String usuario, String nombre, String clave, String roles) {
}
