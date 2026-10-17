package com.example.hito2grupo3.repositories;

import com.example.hito2grupo3.entities.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByDni(String dni);
    Optional<Staff> findByUsuario_Email(String email);
}
