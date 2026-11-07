package com.example.hito2grupo3.services;

import com.example.hito2grupo3.configuration.CanonProperties;
import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.PuestoDesarmable;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.impl.CanonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CanonServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private UnidadVentaRepository unidadVentaRepository;

    private CanonProperties canonProperties;
    private CanonServiceImpl canonService;

    @BeforeEach
    void setUp() {
        canonProperties = new CanonProperties(
                new BigDecimal("500"),
                new BigDecimal("2000"),
                new BigDecimal("10")
        );
        canonService = new CanonServiceImpl(staffRepository, unidadVentaRepository, canonProperties);
    }

    @Test
    void calcularCanonFoodTruckConElectricidad() {
        FoodTruck foodTruck = new FoodTruck();
        foodTruck.setSuperficieM2(new BigDecimal("20"));
        foodTruck.setRequiereElectricidad(true);

        // 20 m2 * $500 + $2000 = $12000
        BigDecimal canon = canonService.calcularCanon(foodTruck);

        assertEquals(new BigDecimal("12000"), canon);
    }

    @Test
    void calcularCanonFoodTruckSinElectricidad() {
        FoodTruck foodTruck = new FoodTruck();
        foodTruck.setSuperficieM2(new BigDecimal("15"));
        foodTruck.setRequiereElectricidad(false);

        // 15 m2 * $500 = $7500
        BigDecimal canon = canonService.calcularCanon(foodTruck);

        assertEquals(new BigDecimal("7500"), canon);
    }

    @Test
    void calcularCanonPuestoDesarmable() {
        PuestoDesarmable puesto = new PuestoDesarmable();
        puesto.setSuperficieM2(new BigDecimal("30"));
        puesto.setTiempoMontajeMin(60);

        // 30 m2 * $500 - (60 * $10) = 15000 - 600 = $14400
        BigDecimal canon = canonService.calcularCanon(puesto);

        assertEquals(new BigDecimal("14400"), canon);
    }

    @Test
    void calcularCanonPuestoDesarmableNoPuedeSerNegativo() {
        PuestoDesarmable puesto = new PuestoDesarmable();
        puesto.setSuperficieM2(new BigDecimal("1"));
        puesto.setTiempoMontajeMin(100);

        // 1 m2 * $500 - (100 * $10) = 500 - 1000 = -500 -> 0
        BigDecimal canon = canonService.calcularCanon(puesto);

        assertEquals(BigDecimal.ZERO, canon);
    }

    @Test
    void calcularCanonConValoresPersonalizadosDeProperties() {
        CanonProperties customProps = new CanonProperties(
                new BigDecimal("600"),
                new BigDecimal("3000"),
                new BigDecimal("15")
        );
        CanonServiceImpl customService = new CanonServiceImpl(staffRepository, unidadVentaRepository, customProps);

        FoodTruck foodTruck = new FoodTruck();
        foodTruck.setSuperficieM2(new BigDecimal("10"));
        foodTruck.setRequiereElectricidad(true);

        // 10 m2 * $600 + $3000 = $9000
        BigDecimal canon = customService.calcularCanon(foodTruck);

        assertEquals(new BigDecimal("9000"), canon);
    }
}

