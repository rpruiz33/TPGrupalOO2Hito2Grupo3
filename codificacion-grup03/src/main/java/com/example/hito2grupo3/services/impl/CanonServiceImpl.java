
package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.PuestoDesarmable;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.services.ICanonService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CanonServiceImpl implements ICanonService {

    private static final BigDecimal VALOR_M2_BASE = BigDecimal.valueOf(500);
    private static final BigDecimal DESCUENTO_MIN_MONTAJE = BigDecimal.valueOf(10);
    private static final BigDecimal PLUS_ELECTRICIDAD = BigDecimal.valueOf(2000);

    private final StaffRepository staffRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public CanonServiceImpl(
            StaffRepository staffRepository,
            UnidadVentaRepository unidadVentaRepository) {
        this.staffRepository = staffRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @Override
    public BigDecimal calcularCanon(UnidadVenta unidad) {

        if (unidad == null) {
            return BigDecimal.ZERO;
        }

        if (unidad instanceof PuestoDesarmable puesto) {
            return calcularCanonPuestoDesarmable(puesto);
        }

        if (unidad instanceof FoodTruck foodTruck) {
            return calcularCanonFoodTruck(foodTruck);
        }

        throw new IllegalArgumentException(
                "Tipo de unidad de venta no reconocido: "
                        + unidad.getClass().getSimpleName());
    }

    @Override
    public BigDecimal calcularCanonPuestoDesarmable(PuestoDesarmable puesto) {

        if (puesto == null || puesto.getSuperficieM2() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal canonBase =
                puesto.getSuperficieM2().multiply(VALOR_M2_BASE);

        long minutos = puesto.getTiempoMontajeMin() != null
                ? puesto.getTiempoMontajeMin() : 0L;

        BigDecimal descuento =
                BigDecimal.valueOf(minutos).multiply(DESCUENTO_MIN_MONTAJE);

        BigDecimal canonFinal = canonBase.subtract(descuento);

        return canonFinal.compareTo(BigDecimal.ZERO) < 0
                ? BigDecimal.ZERO : canonFinal;
    }

    @Override
    public BigDecimal calcularCanonFoodTruck(FoodTruck foodTruck) {

        if (foodTruck == null || foodTruck.getSuperficieM2() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal canon =
                foodTruck.getSuperficieM2().multiply(VALOR_M2_BASE);

        if (foodTruck.isRequiereElectricidad()) {
            canon = canon.add(PLUS_ELECTRICIDAD);
        }

        return canon;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularCanonPorResponsableEmail(String email) {

        if (email == null || email.isBlank()) {
            return BigDecimal.ZERO;
        }

        Staff responsable = staffRepository
                .findByUsuario_Email(email.trim())
                .orElse(null);

        if (responsable == null) {
            return BigDecimal.ZERO;
        }

        UnidadVenta unidad = unidadVentaRepository
                .findByResponsable_Id(responsable.getId())
                .orElse(null);

        return calcularCanon(unidad);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularCanonPorUnidadId(Long unidadId) {

        if (unidadId == null) {
            return BigDecimal.ZERO;
        }

        UnidadVenta unidad = unidadVentaRepository
                .findById(unidadId)
                .orElse(null);

        return calcularCanon(unidad);
    }
}
