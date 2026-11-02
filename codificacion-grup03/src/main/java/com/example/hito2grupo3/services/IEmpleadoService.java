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

    /**
     * Busca el Staff asociado al email de usuario indicado.
     * Retorna {@code null} si no existe.
     */
    Staff buscarPorEmail(String email);

    /**
     * Obtiene el nombre completo del empleado asociado al email indicado.
     * Retorna "Responsable" si no se encuentra.
     */
    String obtenerNombreCompletoPorEmail(String email);

    /**
     * Dado un ID que puede corresponder a un Usuario o a un Staff,
     * resuelve y devuelve el Staff correspondiente.
     * Crea un Staff vacío vinculado al usuario si no existe staff previo.
     *
     * @return el Staff resuelto
     * @throws IllegalArgumentException si no existe ni usuario ni staff con ese ID
     */
    Staff resolverStaffDesdeId(Long id);

    /**
     * Dado el Staff del creador (responsable), asocia el nuevoStaff
     * a la unidad de venta del creador (si existe).
     *
     * @param creadorEmail email del usuario que está creando al empleado
     * @param nuevoStaff   el Staff recién creado
     */
    void asociarEmpleadoAUnidadDelCreador(String creadorEmail, Staff nuevoStaff);

    /**
     * Dado un ID (puede ser de Usuario o de Staff), resuelve el ID de Usuario
     * que se utilizará para reenviar credenciales.
     */
    Long resolverUsuarioId(Long id);

    /**
     * Elimina o da de baja al usuario y su staff asociado según el ID provisto.
     * El ID puede ser de un Usuario o de un Staff.
     */
    void eliminarODarDeBajaPorId(Long id);

    /**
     * Filtra y retorna la lista de empleados de una unidad de venta aplicando
     * los criterios: nombre, apellido, DNI, estado (ACTIVOS/INACTIVOS/TODOS),
     * rol y email.
     *
     * @param unidadId  ID de la unidad de venta
     * @param nombre    filtro parcial por nombre (puede ser nulo)
     * @param apellido  filtro parcial por apellido (puede ser nulo)
     * @param dni       filtro parcial por DNI (puede ser nulo)
     * @param estado    ACTIVOS, INACTIVOS o TODOS
     * @param rol       filtro exacto por rol (puede ser nulo)
     * @param email     filtro parcial por email (puede ser nulo)
     * @return lista de Staff filtrada
     */
    java.util.List<com.example.hito2grupo3.entities.Staff> filtrarEmpleadosPorUnidad(
            Long unidadId,
            String nombre,
            String apellido,
            String dni,
            String estado,
            String rol,
            String email
    );
}
