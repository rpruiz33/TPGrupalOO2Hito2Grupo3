package com.example.hito2grupo3.repositories;

import com.example.hito2grupo3.entities.Cocinero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CocineroRepository extends JpaRepository<Cocinero, Long> {
    Optional<Cocinero> findByDni(String dni);
    boolean existsByCategoriaCocina_Id(Long categoriaCocinaId);
    long countByCategoriaCocina_Id(Long categoriaCocinaId);
    List<Cocinero> findByCategoriaCocina_Id(Long categoriaCocinaId);
}
