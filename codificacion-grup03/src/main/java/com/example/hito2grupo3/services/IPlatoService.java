package com.example.hito2grupo3.services;

import com.example.hito2grupo3.entities.Plato;
import com.example.hito2grupo3.entities.UnidadVenta;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de negocio para la gestión de platos de una unidad de venta.
 */
public interface IPlatoService {

    /**
     * Lista todos los platos de una unidad de venta, ordenados por nombre.
     */
    List<Plato> listarPorUnidad(Long unidadVentaId);

    /**
     * Retorna un plato por su ID, verificando que pertenezca a la unidad del responsable.
     *
     * @throws IllegalArgumentException si no existe o no pertenece a la unidad
     */
    Plato buscarPorIdYUnidad(Long platoId, Long unidadVentaId);

    /**
     * Registra un nuevo plato para la unidad de venta indicada, aplicando las
     * reglas de negocio (precio > costo de producción).
     *
     * @param plato         datos del plato a crear (sin ID ni unidad asignados)
     * @param unidadVentaId ID de la unidad de venta propietaria
     * @return el plato persistido
     * @throws IllegalArgumentException si el precio no supera el costo de producción
     */
    Plato crearPlato(Plato plato, Long unidadVentaId);

    /**
     * Actualiza los datos editables de un plato existente (nombre, precio, costo,
     * estado activo), verificando que pertenezca a la unidad indicada y que el
     * precio siga siendo mayor al costo de producción.
     *
     * @param platoId       ID del plato a actualizar
     * @param datos         nuevos datos del plato
     * @param unidadVentaId ID de la unidad de venta propietaria
     * @return el plato actualizado
     * @throws IllegalArgumentException si no existe, no pertenece a la unidad o el
     *                                  precio no supera el costo
     */
    Plato actualizarPlato(Long platoId, Plato datos, Long unidadVentaId);

    /**
     * Valida la regla de negocio: el precio de venta debe ser mayor al costo de
     * producción.
     *
     * @throws IllegalArgumentException si la regla no se cumple
     */
    void validarPrecioVsCosto(BigDecimal precio, BigDecimal costoProduccion);

    /**
     * Obtiene la unidad de venta de la que es responsable el usuario autenticado.
     *
     * @param email email del usuario autenticado
     * @return la UnidadVenta, o {@code null} si no existe asociación
     */
    UnidadVenta obtenerUnidadDelResponsable(String email);
}
