package com.example.Actividad01.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Actividad01.entity.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {
}
