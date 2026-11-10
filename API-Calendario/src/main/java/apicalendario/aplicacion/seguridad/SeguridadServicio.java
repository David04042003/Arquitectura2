package apicalendario.aplicacion.seguridad;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import apicalendario.core.interfaces.servicios.ITokenServicio;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Genera y valida los tokens JWT firmados con HS256.
 */
@Component
public class SeguridadServicio implements ITokenServicio {

    private final SecretKey claveFirma;
    private final Duration vigencia;

    public SeguridadServicio(PropiedadesJwt propiedades) {
        this.claveFirma = Keys.hmacShaKeyFor(Decoders.BASE64.decode(propiedades.secreto()));
        this.vigencia = Duration.ofMinutes(propiedades.expiracionMinutos());
    }

    @Override
    public String generarToken(String nombreUsuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(nombreUsuario)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(vigencia)))
                .signWith(claveFirma, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * El parser verifica la firma y la fecha de expiración; si algo falla lanza una JwtException.
     */
    @Override
    public String extraerNombreUsuario(String token) {
        return Jwts.parser()
                .verifyWith(claveFirma)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
