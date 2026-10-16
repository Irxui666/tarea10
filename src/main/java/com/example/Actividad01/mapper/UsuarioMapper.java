package com.example.Actividad01.mapper;

import org.springframework.stereotype.Component;
import com.example.Actividad01.dto.auth.EmpleadoResponseDTO;
import com.example.Actividad01.dto.auth.RolResponseDTO;
import com.example.Actividad01.dto.auth.UsuarioResponseDTO;
import com.example.Actividad01.entity.Empleado;
import com.example.Actividad01.entity.Rol;
import com.example.Actividad01.entity.Usuario;
import com.example.Actividad01.security.PermisosPorRol;

import java.util.Collection;
import java.util.List;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toUsuarioResponse(Usuario usuario) {
        Empleado empleado = usuario.getEmpleado();
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                empleado.getId(),
                empleado.getNombreCompleto(),
                empleado.getCargo(),
                nombresRoles(usuario.getRoles()),
                PermisosPorRol.permisos(usuario.getRoles()),
                usuario.getEstado(),
                usuario.getFechaCreacion()
        );
    }

    public EmpleadoResponseDTO toEmpleadoResponse(Empleado empleado, boolean tieneUsuario) {
        return new EmpleadoResponseDTO(
                empleado.getId(),
                empleado.getDni(),
                empleado.getNombres(),
                empleado.getApellidos(),
                empleado.getCargo(),
                empleado.getEmail(),
                empleado.getTelefono(),
                empleado.getEstado(),
                tieneUsuario
        );
    }

    public RolResponseDTO toRolResponse(Rol rol) {
        return new RolResponseDTO(
                rol.getId(),
                rol.getNombre(),
                rol.getDescripcion(),
                PermisosPorRol.permisos(List.of(rol))
        );
    }

    public List<String> nombresRoles(Collection<Rol> roles) {
        return roles.stream()
                .map(Rol::getNombre)
                .sorted()
                .toList();
    }
}
