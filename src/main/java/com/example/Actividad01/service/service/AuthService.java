package com.example.Actividad01.service.service;

import com.example.Actividad01.dto.auth.LoginRequestDTO;
import com.example.Actividad01.dto.auth.LoginResponseDTO;
import com.example.Actividad01.dto.auth.UsuarioResponseDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO request);

    UsuarioResponseDTO usuarioActual(String username);
}
