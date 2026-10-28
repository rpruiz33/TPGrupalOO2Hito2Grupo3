package com.example.hito2grupo3.services;

import com.example.hito2grupo3.dto.EmpleadoDTO;
import com.example.hito2grupo3.entities.Staff;

import java.util.List;

public interface IEmpleadoService {

    List<Staff> listarTodos();

    Staff buscarPorId(Long id);

    EmpleadoDTO obtenerDtoPorId(Long id);

    Staff crearEmpleado(EmpleadoDTO dto);

    Staff actualizarEmpleado(Long id, EmpleadoDTO dto);

    void darDeBaja(Long id);

    void reactivar(Long id);

    void reenviarCredenciales(Long id);
}
