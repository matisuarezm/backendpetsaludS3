package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Collection;

/**
 * Adaptador entre nuestra entidad Usuario y Spring Security.
 * Spring no conoce "Usuario", solo "UserDetails": esta clase le traduce email, clave cifrada y rol.
 */
public class UsuarioDetails implements UserDetails {

    private final Usuario usuario;

    public UsuarioDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Entrega el rol del usuario (CLIENTE, EMPLEADO o GERENTE) como permiso.
     * Es lo que comparan hasAuthority() en SecurityConfig y @PreAuthorize en los controllers.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(usuario.getRol().name()));
    }

    /**
     * Entrega la contraseña cifrada con BCrypt guardada en la BD.
     */
    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    /**
     * Entrega el email, que usamos como nombre de usuario.
     */
    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    /**
     * Estos cuatro métodos indican si la cuenta está vigente, desbloqueada, con clave vigente y habilitada.
     * Por ahora siempre responden true.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // Puedes implementar lógica adicional si es necesario
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Puedes implementar lógica adicional si es necesario
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Puedes implementar lógica adicional si es necesario
    }

    @Override
    public boolean isEnabled() {
        return true; // Puedes implementar lógica adicional si es necesario
    }

    /**
     * Entrega el id del usuario logueado. Lo usa la clase Propiedad
     * para comparar si un recurso le pertenece.
     */
    public Long getId() {
        return usuario.getId();
    }
}
