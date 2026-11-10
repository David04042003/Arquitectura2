package apicalendario.core.dtos;

/**
 * Respuesta del login: el usuario autenticado y su token de seguridad (Bearer).
 */
public record UsuarioLoginDto(UsuarioDto usuario, String token) {
}
