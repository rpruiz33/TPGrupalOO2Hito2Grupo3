package com.example.hito2grupo3.repositories;

import com.example.hito2grupo3.entities.Cajero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CajeroRepository extends JpaRepository<Cajero, Long> {
    Optional<Cajero> findByDni(String dni);
}
