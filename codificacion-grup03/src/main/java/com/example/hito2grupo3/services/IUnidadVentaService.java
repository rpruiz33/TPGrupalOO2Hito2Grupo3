package com.example.hito2grupo3.services;

import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.PuestoDesarmable;
import com.example.hito2grupo3.entities.UnidadVenta;

import java.math.BigDecimal;
import java.util.List;

public interface IUnidadVentaService {

    /**
     * Calcula el canon a pagar por una unidad de venta polimórficamente
     * según su tipo concreto (FoodTruck o PuestoDesarmable).
     */
    BigDecimal calcularCanon(UnidadVenta unidad);

    /**
     * Lógica de negocio específica para cálculo de canon de Food Truck:
     * (areaSup * $500) + $2000 si requiere conexión eléctrica.
     */
    BigDecimal calcularCanonFoodTruck(FoodTruck foodTruck);

    /**
     * Lógica de negocio específica para cálculo de canon de Puesto Desarmable:
     * (areaSup * $500) - (tiempoMontaje * $10).
     */
    BigDecimal calcularCanonPuestoDesarmable(PuestoDesarmable puestoDesarmable);

    /**
     * Obtiene la unidad asociada a un Responsable y calcula su canon.
     */
    BigDecimal calcularCanonPorResponsableEmail(String email);

    /**
     * Obtiene una unidad de venta por su ID y calcula su canon.
     */
    BigDecimal calcularCanonPorUnidadId(Long unidadId);

    /**
     * Valida el formato del código de una unidad de venta según su subtipo concreto.
     */
    boolean validarCodigo(UnidadVenta unidad);

    /**
     * Valida si un código cumple con el formato según el tipo especificado ("FOOD_TRUCK" o "PUESTO_DESARMABLE").
     */
    boolean esCodigoValido(String codigo, String tipoUnidad);

    /**
     * Valida si un código cumple el formato específico de Food Truck (ej: FT-0000001).
     */
    boolean validarCodigoFoodTruck(String codigo);

    /**
     * Valida si un código cumple el formato específico de Puesto Desarmable (ej: PD-0000001).
     */
    boolean validarCodigoPuestoDesarmable(String codigo);

    /**
     * Busca la unidad de venta asociada a un responsable por su email de usuario.
     */
    UnidadVenta buscarPorResponsableEmail(String email);

    /**
     * Busca una unidad de venta por su ID.
     */
    UnidadVenta buscarPorId(Long id);

    /**
     * Obtiene el listado completo de unidades de venta.
     */
    List<UnidadVenta> listarTodas();

    void darDeBaja(Long id);
}
