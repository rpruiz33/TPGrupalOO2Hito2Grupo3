package com.example.hito2grupo3.repositories;

import com.example.hito2grupo3.entities.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByDni(String dni);

    Optional<Staff> findByUsuario_Email(String email);

    Optional<Staff> findByUsuario_Id(Long usuarioId);

    @Query("SELECT s FROM Staff s WHERE s.usuario.id = :usuarioId")
    Optional<Staff> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Actualización directa del campo activo en la tabla staff.
     * Evita problemas con InheritanceType.JOINED al no pasar por save().
     */
    @Modifying
    @Query("UPDATE Staff s SET s.activo = :activo WHERE s.id = :id")
    int actualizarActivo(@Param("id") Long id, @Param("activo") boolean activo);

    @Query("""
        SELECT s
        FROM UnidadVenta uv
        JOIN uv.staffAsignado s
        LEFT JOIN FETCH s.usuario
        WHERE uv.id = :unidadId
          AND s.activo = true
          AND (:nombre IS NULL OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND (:apellido IS NULL OR LOWER(s.apellido) LIKE LOWER(CONCAT('%', :apellido, '%')))
          AND (:dni IS NULL OR s.dni LIKE CONCAT('%', :dni, '%'))
        ORDER BY s.apellido, s.nombre
        """)
    List<Staff> filtrarActivosPorUnidad(
            @Param("unidadId") Long unidadId,
            @Param("nombre") String nombre,
            @Param("apellido") String apellido,
            @Param("dni") String dni
    );

    @Query("""
        SELECT s
        FROM UnidadVenta uv
        JOIN uv.staffAsignado s
        LEFT JOIN FETCH s.usuario
        WHERE uv.id = :unidadId
          AND (:nombre IS NULL OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND (:apellido IS NULL OR LOWER(s.apellido) LIKE LOWER(CONCAT('%', :apellido, '%')))
          AND (:dni IS NULL OR s.dni LIKE CONCAT('%', :dni, '%'))
        ORDER BY s.apellido, s.nombre
        """)
    List<Staff> filtrarPorUnidad(
            @Param("unidadId") Long unidadId,
            @Param("nombre") String nombre,
            @Param("apellido") String apellido,
            @Param("dni") String dni
    );
}
