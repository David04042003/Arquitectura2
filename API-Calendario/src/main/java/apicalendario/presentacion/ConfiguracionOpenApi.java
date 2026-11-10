package apicalendario.presentacion;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Documentación Swagger (http://localhost:8081/swagger-ui.html) con el botón "Authorize" para el token.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API Calendario",
        version = "1.0.0",
        description = "Clasifica los días del año (laboral, fin de semana, festivo) consumiendo la API Festivos. "
                + "Arquitectura Onion - Spring Boot - PostgreSQL - Seguridad con token JWT."))
@SecurityScheme(
        name = ConfiguracionOpenApi.ESQUEMA_SEGURIDAD,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class ConfiguracionOpenApi {

    public static final String ESQUEMA_SEGURIDAD = "token";
}
