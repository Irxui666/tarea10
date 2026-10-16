package com.example.Actividad01.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.Actividad01.dto.auth.EmpleadoResponseDTO;
import com.example.Actividad01.dto.auth.RolResponseDTO;
import com.example.Actividad01.dto.auth.UsuarioRequestDTO;
import com.example.Actividad01.dto.auth.UsuarioResponseDTO;
import com.example.Actividad01.service.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponseDTO> crear(
            @Valid @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioService.crear(request));
    }

    @GetMapping("/empleados")
    public ResponseEntity<List<EmpleadoResponseDTO>> empleados() {
        return ResponseEntity.ok(usuarioService.listarEmpleados());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RolResponseDTO>> roles() {
        return ResponseEntity.ok(usuarioService.listarRoles());
    }

    @PostMapping("/empleados")
    public ResponseEntity<EmpleadoResponseDTO> crearEmpleado(
            @Valid @RequestBody com.example.Actividad01.dto.auth.EmpleadoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearEmpleado(request));
    }
}
