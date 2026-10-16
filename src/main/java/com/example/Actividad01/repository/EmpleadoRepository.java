package com.example.Actividad01.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Actividad01.entity.Empleado;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    boolean existsByDni(String dni);
    boolean existsByEmailIgnoreCase(String email);
}
