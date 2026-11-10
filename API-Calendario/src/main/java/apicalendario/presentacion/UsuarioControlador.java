package apicalendario.presentacion;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import apicalendario.core.dtos.CredencialesDto;
import apicalendario.core.dtos.UsuarioLoginDto;
import apicalendario.core.interfaces.servicios.IUsuarioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Login: valida las credenciales y devuelve el usuario y el token. No requiere token.
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios")
public class UsuarioControlador {

    private final IUsuarioServicio servicio;

    public UsuarioControlador(IUsuarioServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/validar/{usuario}/{clave}")
    @Operation(summary = "Login con las credenciales en la ruta (formato de la guía del taller)")
    public UsuarioLoginDto validar(@PathVariable String usuario, @PathVariable String clave) {
        return servicio.login(usuario, clave);
    }

    @PostMapping("/login")
    @Operation(summary = "Login con las credenciales en el cuerpo (recomendado: la clave no queda en la URL)")
    public UsuarioLoginDto login(@Valid @RequestBody CredencialesDto credenciales) {
        return servicio.login(credenciales.usuario(), credenciales.clave());
    }
}
