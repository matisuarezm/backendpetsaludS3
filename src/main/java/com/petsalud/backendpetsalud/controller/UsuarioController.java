package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.dto.ActualizarUsuarioRequest;   // ← NUEVO
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.service.UsuarioService;
import jakarta.validation.Valid;                                    // ← NUEVO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Endpoints de usuarios: registro público, consulta y actualización;
 * listar y eliminar solo para GERENTE.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Registro público. @Valid revisa nombre, email, clave, etc. antes de entrar (si fallan: 400).
     * El servicio fuerza el rol CLIENTE y cifra la clave.
     */
    @PostMapping("/registro")
    public ResponseEntity<Usuario> registrarUsuario(@Valid @RequestBody Usuario usuario) { // ← CAMBIO: @Valid
        Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
        return ResponseEntity.ok(nuevoUsuario);
    }

    /**
     * Devuelve un usuario por su id (404 si no existe). La contraseña nunca viaja en la respuesta.
     */
    @PreAuthorize("hasAnyAuthority('EMPLEADO','GERENTE') or @propiedad.esUsuario(#id, authentication)")
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ← CAMBIO: recibe el DTO (solo campos editables) en vez de la entidad completa
    /**
     * Actualiza solo nombre, apellido, teléfono y dirección.
     * Usa un DTO para que nadie cambie su rol, email o clave por esta vía (evita la "asignación masiva").
     */
    // Editar: solo GERENTE o el mismo usuario (un EMPLEADO no edita perfiles ajenos)
    @PreAuthorize("hasAuthority('GERENTE') or @propiedad.esUsuario(#id, authentication)")
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id,
                                                     @Valid @RequestBody ActualizarUsuarioRequest request) {
        // Se copian SOLO los campos permitidos. Si el JSON trae "rol" o "email", se ignoran.
        Usuario datos = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .build();
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, datos);
        return ResponseEntity.ok(usuarioActualizado);
    }

    /**
     * Elimina un usuario. Solo GERENTE: @PreAuthorize revisa el rol antes de ejecutar el método.
     */
    @PreAuthorize("hasAuthority('GERENTE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todos los usuarios. Solo GERENTE.
     */
    @PreAuthorize("hasAuthority('GERENTE')")
    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }
}