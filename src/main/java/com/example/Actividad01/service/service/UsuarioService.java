package com.example.Actividad01.service.service;

import com.example.Actividad01.dto.auth.EmpleadoResponseDTO;
import com.example.Actividad01.dto.auth.RolResponseDTO;
import com.example.Actividad01.dto.auth.UsuarioRequestDTO;
import com.example.Actividad01.dto.auth.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {

    UsuarioResponseDTO crear(UsuarioRequestDTO request);

    List<UsuarioResponseDTO> listar();

    List<EmpleadoResponseDTO> listarEmpleados();

    EmpleadoResponseDTO crearEmpleado(com.example.Actividad01.dto.auth.EmpleadoRequestDTO request);

    List<RolResponseDTO> listarRoles();
}
