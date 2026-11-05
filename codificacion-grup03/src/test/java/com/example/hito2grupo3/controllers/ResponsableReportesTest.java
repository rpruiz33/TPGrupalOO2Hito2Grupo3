package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.services.IReporteVentaService;
import com.example.hito2grupo3.services.IRentabilidadNetaService;
import com.example.hito2grupo3.services.IUnidadVentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResponsableReportesTest {

    @Mock
    private IReporteVentaService reporteVentaService;

    @Mock
    private IRentabilidadNetaService rentabilidadNetaService;

    @Mock
    private IUnidadVentaService unidadVentaService;

    @Mock
    private Authentication authentication;

    private ResponsableReporteController responsableReporteController;
    private RentabilidadNetaController rentabilidadNetaController;

    private UnidadVenta unidadResponsable;
    private Festival festival;

    @BeforeEach
    void setUp() {
        responsableReporteController = new ResponsableReporteController(
                reporteVentaService,
                rentabilidadNetaService,
                unidadVentaService
        );

        rentabilidadNetaController = new RentabilidadNetaController(
                rentabilidadNetaService,
                unidadVentaService
        );

        unidadResponsable = new UnidadVenta() {};
        unidadResponsable.setId(50L);
        unidadResponsable.setNombreComercial("Mi Unidad Responsable");
        unidadResponsable.setCodigo("PD-0000050");

        festival = new Festival();
        festival.setId(2L);
        festival.setNombre("Festival Primavera 2026");
        festival.setFechaInicio(LocalDate.of(2026, 9, 1));
        festival.setFechaFin(LocalDate.of(2026, 9, 15));
    }

    @Test
    void recaudacionResponsable_restringidaASuUnidadYPermiteSeleccionarFestival() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("responsable@epicentro.com");
        when(unidadVentaService.buscarPorResponsableEmail("responsable@epicentro.com")).thenReturn(unidadResponsable);
        when(rentabilidadNetaService.listarFestivales()).thenReturn(List.of(festival));

        ReporteVentaDTO reporteMock = new ReporteVentaDTO(2L, "Festival Primavera 2026", new BigDecimal("120000"), 10L);
        RankingUnidadDTO rankingMock = new RankingUnidadDTO(50L, "Mi Unidad Responsable", "PD-0000050", "PuestoDesarmable", new BigDecimal("120000"));

        // Verificamos que se consulta usando el id de la unidad del responsable (50L) y festival seleccionado (2L)
        when(reporteVentaService.obtenerTotalesFestivalYUnidad(2L, 50L)).thenReturn(reporteMock);
        when(reporteVentaService.obtenerRankingFestivalYUnidad(2L, 50L)).thenReturn(List.of(rankingMock));

        Model model = new ConcurrentModel();
        String vista = responsableReporteController.recaudacion(2L, authentication, model);

        assertEquals("reportes/ReporteRecaudacion", vista);
        assertTrue((Boolean) model.getAttribute("esResponsable"));
        assertEquals(2L, model.getAttribute("festivalId"));
        assertEquals(reporteMock, model.getAttribute("reporte"));
        assertEquals(List.of(rankingMock), model.getAttribute("ranking"));
        assertEquals(unidadResponsable, model.getAttribute("unidadVenta"));

        verify(reporteVentaService).obtenerTotalesFestivalYUnidad(2L, 50L);
        verify(reporteVentaService).obtenerRankingFestivalYUnidad(2L, 50L);
    }

    @Test
    void rentabilidadResponsable_restringidaASuUnidadYPermiteSeleccionarFestival() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("responsable@epicentro.com");
        when(unidadVentaService.buscarPorResponsableEmail("responsable@epicentro.com")).thenReturn(unidadResponsable);
        when(rentabilidadNetaService.listarFestivales()).thenReturn(List.of(festival));

        LocalDate desde = LocalDate.of(2026, 9, 1);
        LocalDate hasta = LocalDate.of(2026, 9, 10);

        Map<String, Object> reporteMap = Map.of(
                "totalIngresos", new BigDecimal("200000"),
                "totalCostosPlatos", new BigDecimal("50000"),
                "totalSueldos", new BigDecimal("40000"),
                "totalCanon", new BigDecimal("10000"),
                "totalCostos", new BigDecimal("100000"),
                "rentabilidadNeta", new BigDecimal("100000"),
                "detalles", Collections.emptyList()
        );

        when(rentabilidadNetaService.obtenerReporteRentabilidad(2L, 50L, desde, hasta)).thenReturn(reporteMap);

        Model model = new ConcurrentModel();
        String vista = rentabilidadNetaController.calcularRentabilidadResponsable(
                2L, desde, hasta, authentication, model);

        assertEquals("reportes/rentabilidadNeta", vista);
        assertTrue((Boolean) model.getAttribute("esResponsable"));
        assertFalse((Boolean) model.getAttribute("esAdministrador"));
        assertEquals(2L, model.getAttribute("festivalId"));
        assertEquals(50L, model.getAttribute("unidadVentaId"));
        assertEquals(new BigDecimal("100000"), model.getAttribute("rentabilidadNeta"));

        // Se verifica que la llamada a rentabilidad se hace con la unidad 50L del responsable
        verify(rentabilidadNetaService).obtenerReporteRentabilidad(2L, 50L, desde, hasta);
        // El ranking global de unidades NO debe ser cargado para el Responsable
        verify(rentabilidadNetaService, never()).obtenerRankingUnidades(anyLong());
    }
}

