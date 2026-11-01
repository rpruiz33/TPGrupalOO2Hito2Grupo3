package com.example.hito2grupo3.services;

import com.example.hito2grupo3.entities.UnidadVenta;
import java.math.BigDecimal;

public interface ICanonService {

    /**
     * Calcula el canon correspondiente según las reglas de negocio
     * del tipo de unidad de venta (Puesto Desarmable o Food Truck).
     *
     * @param unidadVenta Entidad de la unidad a calcular
     * @return Importe final del canon en BigDecimal
     */
    BigDecimal calcularCanon(UnidadVenta unidadVenta);
}