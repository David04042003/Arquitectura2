package apicalendario.aplicacion.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

class SeguridadServicioTest {

    private static final String SECRETO = "nQ7w588Alrd8kAThalC6P0c8iLXKxFSowKVbB+r7Ybc=";
    private static final String OTRO_SECRETO = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    @Test
    void elTokenGeneradoContieneElUsuario() {
        SeguridadServicio servicio = new SeguridadServicio(new PropiedadesJwt(SECRETO, 30));
        String token = servicio.generarToken("admin");
        assertEquals("admin", servicio.extraerNombreUsuario(token));
    }

    @Test
    void rechazaTokensFirmadosConOtraClave() {
        String token = new SeguridadServicio(new PropiedadesJwt(OTRO_SECRETO, 30)).generarToken("admin");
        SeguridadServicio servicio = new SeguridadServicio(new PropiedadesJwt(SECRETO, 30));
        assertThrows(JwtException.class, () -> servicio.extraerNombreUsuario(token));
    }

    @Test
    void rechazaTokensExpirados() {
        SeguridadServicio servicio = new SeguridadServicio(new PropiedadesJwt(SECRETO, -1));
        String token = servicio.generarToken("admin");
        assertThrows(ExpiredJwtException.class, () -> servicio.extraerNombreUsuario(token));
    }
}
