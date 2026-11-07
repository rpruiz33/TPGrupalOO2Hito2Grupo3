
package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.configuration.CanonProperties;
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

    private final StaffRepository staffRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final CanonProperties canonProperties;

    @org.springframework.beans.factory.annotation.Autowired
    public CanonServiceImpl(
            StaffRepository staffRepository,
            UnidadVentaRepository unidadVentaRepository,
            CanonProperties canonProperties) {
        this.staffRepository = staffRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.canonProperties = canonProperties != null
                ? canonProperties
                : new CanonProperties(null, null, null);
    }

    public CanonServiceImpl(
            StaffRepository staffRepository,
            UnidadVentaRepository unidadVentaRepository) {
        this(staffRepository, unidadVentaRepository, new CanonProperties(null, null, null));
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
                puesto.getSuperficieM2().multiply(canonProperties.costoPorM2OrDefault());

        long minutos = puesto.getTiempoMontajeMin() != null
                ? puesto.getTiempoMontajeMin() : 0L;

        BigDecimal descuento =
                BigDecimal.valueOf(minutos).multiply(canonProperties.costoPorMinutoMontajeOrDefault());

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
                foodTruck.getSuperficieM2().multiply(canonProperties.costoPorM2OrDefault());

        if (foodTruck.isRequiereElectricidad()) {
            canon = canon.add(canonProperties.plusElectricidadOrDefault());
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
