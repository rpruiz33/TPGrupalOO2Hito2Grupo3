package com.example.hito2grupo3.controllers.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteRentabilidadNetaDTO;
import com.example.hito2grupo3.dto.api.DashboardResumenResponseDTO;
import com.example.hito2grupo3.services.api.IRentabilidadApiService;

@ExtendWith(MockitoExtension.class)
class RentabilidadRestControllerTest {

    @Mock
    private IRentabilidadApiService rentabilidadApiService;

    private MockMvc mockMvc;
    private ReporteRentabilidadNetaDTO rentabilidadEjemplo;

    @BeforeEach
    void setUp() {
        RentabilidadRestController controller = new RentabilidadRestController(rentabilidadApiService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        rentabilidadEjemplo = new ReporteRentabilidadNetaDTO(
                1L,
                "Epicentro Gourmet Primavera",
                1L,
                "Sabores sobre Ruedas",
                "FT-0000001",
                "FoodTruck",
                new BigDecimal("300000.00"),
                new BigDecimal("100000.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("6000.00"),
                new BigDecimal("156000.00"),
                new BigDecimal("144000.00"),
                15L,
                "01/11/2026 - 15/11/2026");
    }

    @Test
    void calcularRentabilidad_retorna200() throws Exception {
        when(rentabilidadApiService.calcularRentabilidadUnidad(any())).thenReturn(rentabilidadEjemplo);

        String body = """
                {
                  "festivalId": 1,
                  "unidadVentaId": 1,
                  "fechaDesde": "01-11-2026",
                  "fechaHasta": "15-11-2026"
                }
                """;

        mockMvc.perform(post("/api/v1/rentabilidad/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentabilidadNeta").value(144000.00));
    }

    @Test
    void consultarRentabilidad_conMultiplesFiltros_retorna200() throws Exception {
        when(rentabilidadApiService.buscarRentabilidadFiltrada(
                eq(1L),
                eq(1L),
                eq("FoodTruck"),
                eq("Primavera"),
                eq(LocalDate.of(2026, 11, 1)),
                eq(LocalDate.of(2026, 11, 15)),
                eq(new BigDecimal("100000"))))
                .thenReturn(List.of(rentabilidadEjemplo));

        mockMvc.perform(get("/api/v1/rentabilidad")
                        .param("festivalId", "1")
                        .param("unidadVentaId", "1")
                        .param("tipoUnidad", "FoodTruck")
                        .param("temporada", "Primavera")
                        .param("fechaDesde", "01-11-2026")
                        .param("fechaHasta", "15-11-2026")
                        .param("rentabilidadMinima", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoUnidad").value("FoodTruck"));
    }

    @Test
    void consultarRecaudacionYDashboard_retorna200() throws Exception {
        RankingUnidadDTO ranking = new RankingUnidadDTO(
                1L, "Sabores sobre Ruedas", "FT-0000001", "FoodTruck", new BigDecimal("300000.00"));
        DashboardResumenResponseDTO dashboard = new DashboardResumenResponseDTO(
                1L,
                "Epicentro Gourmet Primavera",
                "Primavera",
                new BigDecimal("300000.00"),
                new BigDecimal("156000.00"),
                new BigDecimal("144000.00"),
                1,
                List.of(rentabilidadEjemplo));

        when(rentabilidadApiService.buscarRecaudacionFiltrada(1L, 1L, "Primavera", "FoodTruck"))
                .thenReturn(List.of(ranking));
        when(rentabilidadApiService.obtenerDashboardResumen(1L, "Primavera"))
                .thenReturn(dashboard);

        mockMvc.perform(get("/api/v1/rentabilidad/recaudacion")
                        .param("festivalId", "1")
                        .param("unidadVentaId", "1")
                        .param("temporada", "Primavera")
                        .param("tipoUnidad", "FoodTruck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreComercial").value("Sabores sobre Ruedas"));

        mockMvc.perform(get("/api/v1/rentabilidad/dashboard")
                        .param("festivalId", "1")
                        .param("temporada", "Primavera"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentabilidadNetaTotal").value(144000.00));
    }
}
