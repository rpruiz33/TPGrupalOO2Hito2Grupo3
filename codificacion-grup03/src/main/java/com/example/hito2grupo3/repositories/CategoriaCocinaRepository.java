package com.example.hito2grupo3.repositories;

import com.example.hito2grupo3.entities.CategoriaCocina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaCocinaRepository extends JpaRepository<CategoriaCocina, Long> {
    Optional<CategoriaCocina> findByNombre(String nombre);
}
