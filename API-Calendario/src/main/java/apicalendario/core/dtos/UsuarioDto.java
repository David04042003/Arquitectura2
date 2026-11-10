package apicalendario.core.dtos;

import apicalendario.core.entidades.Usuario;

/**
 * Datos públicos de un usuario (nunca expone la clave).
 */
public record UsuarioDto(Integer id, String usuario, String nombre, String roles) {

    public static UsuarioDto desde(Usuario usuario) {
        return new UsuarioDto(usuario.getId(), usuario.getUsuario(), usuario.getNombre(), usuario.getRoles());
    }
}
