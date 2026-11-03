package com.example.hito2grupo3.services;
import com.example.hito2grupo3.dto.AsignacionStaffDTO;
import java.util.List;
import java.util.Map;

/**
 * Servicio de negocio para construir la vista de información personal
 * de un empleado autenticado (panel "Mi Información").
 */
public interface IEmpleadoInfoService {

    /**
     * Construye el mapa de datos personales y laborales del empleado
     * identificado por el email proporcionado.
     *
     * <p>Incluye: nombre completo, DNI, rol legible, antigüedad calculada,
     * turno y número de caja (si es cajero), email y flag de cajero.</p>
     *
     * @param email email del usuario autenticado
     * @return mapa con los datos del empleado para la vista
     * @throws IllegalArgumentException si no se encuentra staff asociado al email
     */
    Map<String, Object> obtenerDatosEmpleado(String email);

    /**
     * Construye el mapa de la asignación actual del empleado:
     * unidad de venta con festival vigente (si existe).
     *
     * <p>Si no hay asignación vigente, devuelve valores por defecto indicando
     * "Sin unidad con festival vigente" / "Sin festival vigente".</p>
     *
     * @param email email del usuario autenticado
     * @return mapa con "unidad" y "festival" de la asignación actual
     */
    Map<String, Object> obtenerAsignacionActual(String email);

    /**
     * Construye todos los atributos necesarios para el dashboard /mi-informacion del empleado.
     */
    Map<String, Object> obtenerDashboardEmpleado(String email);

    List<AsignacionStaffDTO> obtenerHistorialAsignaciones(String email);
}

