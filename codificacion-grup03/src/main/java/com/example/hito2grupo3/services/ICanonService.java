
package com.example.hito2grupo3.services;

import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.PuestoDesarmable;
import com.example.hito2grupo3.entities.UnidadVenta;

import java.math.BigDecimal;

public interface ICanonService {

    BigDecimal calcularCanon(UnidadVenta unidadVenta);

    BigDecimal calcularCanonFoodTruck(FoodTruck foodTruck);

    BigDecimal calcularCanonPuestoDesarmable(PuestoDesarmable puestoDesarmable);

    BigDecimal calcularCanonPorResponsableEmail(String email);

    BigDecimal calcularCanonPorUnidadId(Long unidadId);
}
