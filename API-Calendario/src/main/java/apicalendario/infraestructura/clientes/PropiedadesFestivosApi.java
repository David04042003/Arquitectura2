package apicalendario.infraestructura.clientes;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración de la conexión a la API Festivos (prefijo "festivos.api" en application.properties).
 */
@ConfigurationProperties(prefix = "festivos.api")
public record PropiedadesFestivosApi(
        String url,
        @DefaultValue("5s") Duration tiempoEspera) {
}
