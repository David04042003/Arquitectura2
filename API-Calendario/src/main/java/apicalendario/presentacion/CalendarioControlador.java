package apicalendario.presentacion;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import apicalendario.core.entidades.Calendario;
import apicalendario.core.interfaces.servicios.ICalendarioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Métodos web del calendario. Requieren token (Bearer).
 */
@RestController
@RequestMapping("/api/calendario")
@Tag(name = "Calendario")
@SecurityRequirement(name = ConfiguracionOpenApi.ESQUEMA_SEGURIDAD)
public class CalendarioControlador {

    private final ICalendarioServicio servicio;

    public CalendarioControlador(ICalendarioServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/generar/{anio}")
    @Operation(summary = "Clasifica y guarda todos los días del año consultando la API Festivos")
    public boolean generar(@PathVariable int anio) {
        return servicio.generar(anio);
    }

    @GetMapping("/listar/{anio}")
    @Operation(summary = "Lista los días guardados del año con su tipo")
    public List<Calendario> listar(@PathVariable int anio) {
        return servicio.listar(anio);
    }
}
