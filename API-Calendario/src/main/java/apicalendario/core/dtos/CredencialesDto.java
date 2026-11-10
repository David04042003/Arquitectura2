package apicalendario.core.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales enviadas en el cuerpo de POST /api/usuarios/login.
 */
public record CredencialesDto(@NotBlank String usuario, @NotBlank String clave) {
}
