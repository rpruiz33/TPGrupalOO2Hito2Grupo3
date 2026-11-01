package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.PuestoDesarmable;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.services.ICanonService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CanonServiceImpl implements ICanonService {

    private static final BigDecimal VALOR_M2_BASE = BigDecimal.valueOf(500);
    private static final BigDecimal DESCUENTO_MIN_MONTAJE = BigDecimal.valueOf(10);
    private static final BigDecimal PLUS_ELECTRICIDAD = BigDecimal.valueOf(2000);

    @Override
    public BigDecimal calcularCanon(UnidadVenta unidad) {
        if (unidad == null) {
            return BigDecimal.ZERO;
        }

        if (unidad instanceof PuestoDesarmable puesto) {
            return calcularCanonPuestoDesarmable(puesto);
        } else if (unidad instanceof FoodTruck foodTruck) {
            return calcularCanonFoodTruck(foodTruck);
        }

        throw new IllegalArgumentException("Tipo de unidad de venta no reconocido: " + unidad.getClass().getSimpleName());
    }

    private BigDecimal calcularCanonPuestoDesarmable(PuestoDesarmable puesto) {
        if (puesto.getSuperficieM2() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal canonBase = puesto.getSuperficieM2().multiply(VALOR_M2_BASE);
        long minutos = puesto.getTiempoMontajeMin() != null ? puesto.getTiempoMontajeMin() : 0L;
        BigDecimal descuento = BigDecimal.valueOf(minutos).multiply(DESCUENTO_MIN_MONTAJE);

        BigDecimal canonFinal = canonBase.subtract(descuento);
        return canonFinal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : canonFinal;
    }

    private BigDecimal calcularCanonFoodTruck(FoodTruck foodTruck) {
        if (foodTruck.getSuperficieM2() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal canon = foodTruck.getSuperficieM2().multiply(VALOR_M2_BASE);

        if (foodTruck.isRequiereElectricidad()) {
            canon = canon.add(PLUS_ELECTRICIDAD);
        }

        return canon;
    }
}