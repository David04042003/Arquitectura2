package apicalendario.infraestructura.clientes;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import apicalendario.core.dtos.FestivoDto;
import apicalendario.core.excepciones.ServicioExternoExcepcion;
import apicalendario.core.interfaces.clientes.IFestivoCliente;

/**
 * Adaptador HTTP: implementa IFestivoCliente consumiendo GET /api/festivos/obtener/{anio} de la API Festivos.
 */
@Component
public class FestivoCliente implements IFestivoCliente {

    private static final ParameterizedTypeReference<List<FestivoDto>> LISTA_FESTIVOS = new ParameterizedTypeReference<>() {
    };

    private final RestClient restClient;
    private final String urlBase;

    public FestivoCliente(RestClient.Builder constructor, PropiedadesFestivosApi propiedades) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(propiedades.tiempoEspera());
        fabrica.setReadTimeout(propiedades.tiempoEspera());

        this.urlBase = propiedades.url();
        this.restClient = constructor
                .baseUrl(propiedades.url())
                .requestFactory(fabrica)
                .build();
    }

    @Override
    public List<FestivoDto> obtenerFestivos(int anio) {
        try {
            List<FestivoDto> festivos = restClient.get()
                    .uri("/api/festivos/obtener/{anio}", anio)
                    .retrieve()
                    .body(LISTA_FESTIVOS);
            return festivos != null ? festivos : List.of();
        } catch (RestClientException ex) {
            throw new ServicioExternoExcepcion(
                    "No fue posible obtener los festivos de " + anio + " desde la API Festivos (" + urlBase + ")", ex);
        }
    }
}
