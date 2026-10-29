package com.example.hito2grupo3.repositories;

import com.example.hito2grupo3.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE UPPER(u.rol.nombre) = 'ADMIN' OR UPPER(u.rol.nombre) = 'ROLE_ADMIN'")
    List<Usuario> findAllAdmins();

    /** Actualización directa del campo activo para el usuario */
    @Modifying
    @Query("UPDATE Usuario u SET u.activo = :activo WHERE u.id = :id")
    int actualizarActivo(@Param("id") Long id, @Param("activo") boolean activo);
}