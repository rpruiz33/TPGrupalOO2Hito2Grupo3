package com.example.hito2grupo3.repositories;

import com.example.hito2grupo3.entities.AsignacionStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionStaffRepository
        extends JpaRepository<AsignacionStaff, Long> {

    List<AsignacionStaff> findByStaff_IdOrderByFechaInicioDesc(Long staffId);

}