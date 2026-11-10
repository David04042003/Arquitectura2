package apicalendario.presentacion;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import apicalendario.aplicacion.seguridad.ConfiguracionSeguridad;
import apicalendario.aplicacion.seguridad.FiltroSeguridad;
import apicalendario.aplicacion.seguridad.SeguridadServicio;
import apicalendario.aplicacion.seguridad.UsuarioDetalleServicio;
import apicalendario.core.dtos.UsuarioDto;
import apicalendario.core.dtos.UsuarioLoginDto;
import apicalendario.core.entidades.Usuario;
import apicalendario.core.excepciones.CredencialesInvalidasExcepcion;
import apicalendario.core.excepciones.SolicitudInvalidaExcepcion;
import apicalendario.core.interfaces.repositorios.IUsuarioRepositorio;
import apicalendario.core.interfaces.servicios.ICalendarioServicio;
import apicalendario.core.interfaces.servicios.IUsuarioServicio;

/**
 * Verifica las reglas de seguridad de los métodos web: sin token 403, con token válido 200.
 */
@WebMvcTest(controllers = { CalendarioControlador.class, UsuarioControlador.class })
@Import({ ConfiguracionSeguridad.class, FiltroSeguridad.class, SeguridadServicio.class, UsuarioDetalleServicio.class })
class SeguridadEndpointsTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SeguridadServicio seguridad;

    @MockitoBean
    private ICalendarioServicio calendarioServicio;

    @MockitoBean
    private IUsuarioServicio usuarioServicio;

    @MockitoBean
    private IUsuarioRepositorio usuarioRepositorio;

    @BeforeEach
    void preparar() {
        when(usuarioRepositorio.obtenerPorUsuario("admin"))
                .thenReturn(Optional.of(new Usuario("admin", "Administrador", "clave-cifrada", "ADMIN")));
        when(calendarioServicio.listar(2023)).thenReturn(List.of());
        when(calendarioServicio.generar(2023)).thenReturn(true);
    }

    @Test
    void sinTokenResponde403() throws Exception {
        mvc.perform(get("/api/calendario/listar/2023"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void conTokenInvalidoResponde403() throws Exception {
        mvc.perform(get("/api/calendario/listar/2023").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("El token no es válido"));
    }

    @Test
    void conTokenValidoResponde200() throws Exception {
        String token = seguridad.generarToken("admin");

        mvc.perform(get("/api/calendario/generar/2023").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
        mvc.perform(get("/api/calendario/listar/2023").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void anioNoNumericoResponde400() throws Exception {
        String token = seguridad.generarToken("admin");
        mvc.perform(get("/api/calendario/listar/abc").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anioFueraDeRangoResponde400() throws Exception {
        when(calendarioServicio.listar(1200)).thenThrow(new SolicitudInvalidaExcepcion("Año no válido"));
        String token = seguridad.generarToken("admin");
        mvc.perform(get("/api/calendario/listar/1200").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Año no válido"));
    }

    @Test
    void loginNoRequiereToken() throws Exception {
        when(usuarioServicio.login("admin", "admin123")).thenReturn(new UsuarioLoginDto(
                new UsuarioDto(1, "admin", "Administrador", "ADMIN"), "token-de-prueba"));

        mvc.perform(get("/api/usuarios/validar/admin/admin123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.usuario").value("admin"))
                .andExpect(jsonPath("$.usuario.clave").doesNotExist())
                .andExpect(jsonPath("$.token").value("token-de-prueba"));
    }

    @Test
    void loginConCredencialesInvalidasResponde401() throws Exception {
        when(usuarioServicio.login(anyString(), anyString())).thenThrow(new CredencialesInvalidasExcepcion());

        mvc.perform(get("/api/usuarios/validar/admin/malaclave"))
                .andExpect(status().isUnauthorized());
    }
}
