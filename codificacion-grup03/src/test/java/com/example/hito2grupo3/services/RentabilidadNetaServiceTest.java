package com.example.hito2grupo3.services;

import com.example.hito2grupo3.dto.ReporteRentabilidadNetaDTO;
import com.example.hito2grupo3.entities.*;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.PedidoRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.impl.RentabilidadNetaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentabilidadNetaServiceTest {

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UnidadVentaRepository unidadVentaRepository;

    @Mock
    private ILiquidacionService liquidacionService;

    @Mock
    private ICanonService canonService;

    private RentabilidadNetaServiceImpl rentabilidadService;
    private Festival festival;
    private FoodTruck foodTruck;

    @BeforeEach
    void setUp() {
        rentabilidadService = new RentabilidadNetaServiceImpl(
                festivalRepository,
                pedidoRepository,
                unidadVentaRepository,
                liquidacionService,
                canonService
        );

        festival = new Festival();
        festival.setId(1L);
        festival.setNombre("Epicentro Gourmet 2026");
        // 20 días de duración total (del 1 al 20 de octubre)
        festival.setFechaInicio(LocalDate.of(2026, 10, 1));
        festival.setFechaFin(LocalDate.of(2026, 10, 20));

        foodTruck = new FoodTruck();
        foodTruck.setId(10L);
        foodTruck.setNombreComercial("Food Truck 1");
        foodTruck.setCodigo("FT-0000001");
        foodTruck.setSuperficieM2(new BigDecimal("20.00"));
        foodTruck.setRequiereElectricidad(true);
    }

    @Test
    void calcularRentabilidadUnidadEntreFechas_sueldosYCanonCorrespondenAlPeriodo() {
        LocalDate desde = LocalDate.of(2026, 10, 1);
        LocalDate hasta = LocalDate.of(2026, 10, 10); // 10 días consultados

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(unidadVentaRepository.findById(10L)).thenReturn(Optional.of(foodTruck));

        // Ingresos y costos de platos en el período
        when(pedidoRepository.sumarIngresosPorFestivalYUnidadYFechas(1L, 10L, desde, hasta))
                .thenReturn(new BigDecimal("300000"));
        when(pedidoRepository.sumarCostosPlatosPorFestivalYUnidadYFechas(1L, 10L, desde, hasta))
                .thenReturn(new BigDecimal("100000"));

        // Días de festival y período
        when(liquidacionService.diasDelFestival(desde, hasta)).thenReturn(10L);
        when(liquidacionService.diasDelFestival(festival.getFechaInicio(), festival.getFechaFin())).thenReturn(20L);

        // Sueldos correspondientes al período consultado (10 días)
        ILiquidacionService.ResultadoLiquidacion liqMock = new ILiquidacionService.ResultadoLiquidacion(
                "Food Truck 1",
                10L,
                Collections.emptySet(),
                new BigDecimal("50000") // Sueldos del período
        );
        when(liquidacionService.liquidarPorUnidadEntreFechas(10L, 1L, desde, hasta)).thenReturn(liqMock);

        // Canon completo = 12000. Para 10 días de 20 días = 12000 * 10 / 20 = 6000
        when(canonService.calcularCanon(foodTruck)).thenReturn(new BigDecimal("12000"));

        when(pedidoRepository.contarPedidosPorFestivalYUnidadYFechas(1L, 10L, desde, hasta)).thenReturn(15L);

        ReporteRentabilidadNetaDTO dto = rentabilidadService.calcularRentabilidadUnidadEntreFechas(
                1L, 10L, desde, hasta);

        assertNotNull(dto);
        assertEquals(0, new BigDecimal("300000").compareTo(dto.getTotalIngresos()));
        assertEquals(0, new BigDecimal("100000").compareTo(dto.getCostosPlatos()));
        // Sueldos del período = 50000
        assertEquals(0, new BigDecimal("50000").compareTo(dto.getSueldos()));
        // Canon proporcional al período = 6000
        assertEquals(0, new BigDecimal("6000.00").compareTo(dto.getCanon()));
        // Total costos = 100000 + 50000 + 6000 = 156000
        assertEquals(0, new BigDecimal("156000.00").compareTo(dto.getTotalCostos()));
        // Rentabilidad Neta = 300000 - 156000 = 144000
        assertEquals(0, new BigDecimal("144000.00").compareTo(dto.getRentabilidadNeta()));
    }

    @Test
    void calcularRentabilidadUnidadEntreFechas_errorEnLiquidacionNoSeReemplazaPorCero() {
        LocalDate desde = LocalDate.of(2026, 10, 1);
        LocalDate hasta = LocalDate.of(2026, 10, 10);

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(unidadVentaRepository.findById(10L)).thenReturn(Optional.of(foodTruck));

        when(pedidoRepository.sumarIngresosPorFestivalYUnidadYFechas(1L, 10L, desde, hasta))
                .thenReturn(new BigDecimal("200000"));
        when(pedidoRepository.sumarCostosPlatosPorFestivalYUnidadYFechas(1L, 10L, desde, hasta))
                .thenReturn(new BigDecimal("50000"));

        when(liquidacionService.diasDelFestival(desde, hasta)).thenReturn(10L);
        when(liquidacionService.diasDelFestival(festival.getFechaInicio(), festival.getFechaFin())).thenReturn(20L);

        // Si la liquidación falla, la excepción debe propagarse y NO ser atrapada reemplazando sueldos por 0
        when(liquidacionService.liquidarPorUnidadEntreFechas(10L, 1L, desde, hasta))
                .thenThrow(new IllegalArgumentException("Error al liquidar personal"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                rentabilidadService.calcularRentabilidadUnidadEntreFechas(1L, 10L, desde, hasta)
        );

        assertEquals("Error al liquidar personal", ex.getMessage());
    }

    @Test
    void calcularRentabilidadUnidadEntreFechas_errorEnCanonNoSeReemplazaPorCero() {
        LocalDate desde = LocalDate.of(2026, 10, 1);
        LocalDate hasta = LocalDate.of(2026, 10, 10);

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(unidadVentaRepository.findById(10L)).thenReturn(Optional.of(foodTruck));

        when(pedidoRepository.sumarIngresosPorFestivalYUnidadYFechas(1L, 10L, desde, hasta))
                .thenReturn(new BigDecimal("200000"));
        when(pedidoRepository.sumarCostosPlatosPorFestivalYUnidadYFechas(1L, 10L, desde, hasta))
                .thenReturn(new BigDecimal("50000"));

        when(liquidacionService.diasDelFestival(desde, hasta)).thenReturn(10L);
        when(liquidacionService.diasDelFestival(festival.getFechaInicio(), festival.getFechaFin())).thenReturn(20L);

        ILiquidacionService.ResultadoLiquidacion liqMock = new ILiquidacionService.ResultadoLiquidacion(
                "Food Truck 1", 10L, Collections.emptySet(), new BigDecimal("40000")
        );
        when(liquidacionService.liquidarPorUnidadEntreFechas(10L, 1L, desde, hasta)).thenReturn(liqMock);

        // Si el canon falla, la excepción debe propagarse y no ser silenciada con 0
        when(canonService.calcularCanon(foodTruck))
                .thenThrow(new IllegalArgumentException("Error al calcular canon"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                rentabilidadService.calcularRentabilidadUnidadEntreFechas(1L, 10L, desde, hasta)
        );

        assertEquals("Error al calcular canon", ex.getMessage());
    }

    @Test
    void calcularRentabilidadUnidadEntreFechas_fechaDesdeAnteriorAlFestival_lanzaExcepcion() {
        // Festival: 2026-10-01 al 2026-10-20
        LocalDate desde = LocalDate.of(2026, 9, 30); // 1 día antes del festival
        LocalDate hasta = LocalDate.of(2026, 10, 10);

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(unidadVentaRepository.findById(10L)).thenReturn(Optional.of(foodTruck));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                rentabilidadService.calcularRentabilidadUnidadEntreFechas(1L, 10L, desde, hasta)
        );

        assertTrue(ex.getMessage().contains("no puede ser anterior al inicio del festival"));
    }

    @Test
    void calcularRentabilidadUnidadEntreFechas_fechaHastaPosteriorAlFestival_lanzaExcepcion() {
        // Festival: 2026-10-01 al 2026-10-20
        LocalDate desde = LocalDate.of(2026, 10, 5);
        LocalDate hasta = LocalDate.of(2026, 10, 25); // 5 días después del cierre

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(unidadVentaRepository.findById(10L)).thenReturn(Optional.of(foodTruck));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                rentabilidadService.calcularRentabilidadUnidadEntreFechas(1L, 10L, desde, hasta)
        );

        assertTrue(ex.getMessage().contains("no puede ser posterior al cierre del festival"));
    }

    @Test
    void calcularRentabilidadUnidadEntreFechas_fechaDesdePosteriorAFechaHasta_lanzaExcepcion() {
        LocalDate desde = LocalDate.of(2026, 10, 15);
        LocalDate hasta = LocalDate.of(2026, 10, 5);

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(unidadVentaRepository.findById(10L)).thenReturn(Optional.of(foodTruck));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                rentabilidadService.calcularRentabilidadUnidadEntreFechas(1L, 10L, desde, hasta)
        );

        assertTrue(ex.getMessage().contains("no puede ser posterior a la fecha 'Hasta'"));
    }

    @Test
    void obtenerReporteRentabilidad_fechaFueraDeRango_lanzaExcepcion() {
        LocalDate desde = LocalDate.of(2026, 9, 15);
        LocalDate hasta = LocalDate.of(2026, 10, 10);

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                rentabilidadService.obtenerReporteRentabilidad(1L, null, desde, hasta)
        );

        assertTrue(ex.getMessage().contains("no puede ser anterior al inicio del festival"));
    }
}

