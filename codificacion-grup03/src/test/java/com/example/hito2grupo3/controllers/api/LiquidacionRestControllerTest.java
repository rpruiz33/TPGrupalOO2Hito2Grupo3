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
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.hito2grupo3.dto.LiquidacionDTO;
import com.example.hito2grupo3.services.ILiquidacionService;
import com.example.hito2grupo3.services.api.ILiquidacionApiService;

@ExtendWith(MockitoExtension.class)
class LiquidacionRestControllerTest {

    @Mock
    private ILiquidacionApiService liquidacionApiService;

    private MockMvc mockMvc;
    private LiquidacionDTO liqEjemplo;
    private ILiquidacionService.ResultadoLiquidacion resumenEjemplo;

    @BeforeEach
    void setUp() {
        LiquidacionRestController controller = new LiquidacionRestController(liquidacionApiService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        liqEjemplo = new LiquidacionDTO(
                4L,
                "Alejandro Ramirez",
                "cajero@epicentro.com",
                "43987651",
                "Cajero",
                3L,
                new BigDecimal("100000.00"),
                new BigDecimal("15000.00"),
                BigDecimal.ZERO,
                new BigDecimal("115000.00"),
                1L,
                "Sabores sobre Ruedas",
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 15),
                15L,
                new BigDecimal("0.5000"),
                10L);

        resumenEjemplo = new ILiquidacionService.ResultadoLiquidacion(
                "Sabores sobre Ruedas",
                15L,
                Set.of(liqEjemplo),
                new BigDecimal("115000.00"));
    }

    @Test
    void registrarLiquidacion_retorna201() throws Exception {
        when(liquidacionApiService.registrarLiquidaciones(any())).thenReturn(resumenEjemplo);

        String body = """
                {
                  "festivalId": 1,
                  "unidadVentaId": 1
                }
                """;

        mockMvc.perform(post("/api/v1/liquidaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalGeneral").value(115000.00));
    }

    @Test
    void buscarHistorial_conMultiplesFiltros_retorna200() throws Exception {
        when(liquidacionApiService.buscarHistorialFiltrado(
                eq(1L),
                eq(1L),
                eq("cajero@epicentro.com"),
                eq("Cajero"),
                eq(LocalDate.of(2026, 11, 1)),
                eq(LocalDate.of(2026, 11, 30)),
                eq(new BigDecimal("50000")),
                eq(new BigDecimal("200000"))))
                .thenReturn(List.of(liqEjemplo));

        mockMvc.perform(get("/api/v1/liquidaciones")
                        .param("festivalId", "1")
                        .param("unidadVentaId", "1")
                        .param("email", "cajero@epicentro.com")
                        .param("rol", "Cajero")
                        .param("fechaDesde", "01-11-2026")
                        .param("fechaHasta", "30-11-2026")
                        .param("montoMinimo", "50000")
                        .param("montoMaximo", "200000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rol").value("Cajero"));
    }
}
