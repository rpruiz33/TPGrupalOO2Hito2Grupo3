package com.example.hito2grupo3.controllers.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.hito2grupo3.dto.api.CierreCajaResponseDTO;
import com.example.hito2grupo3.dto.api.ResumenCajaResponseDTO;
import com.example.hito2grupo3.services.api.ICierreCajaApiService;

@ExtendWith(MockitoExtension.class)
class CierreCajaRestControllerTest {

    @Mock
    private ICierreCajaApiService cierreCajaApiService;

    private MockMvc mockMvc;
    private CierreCajaResponseDTO cierreEjemplo;

    @BeforeEach
    void setUp() {
        CierreCajaRestController controller = new CierreCajaRestController(cierreCajaApiService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        cierreEjemplo = new CierreCajaResponseDTO(
                1L,
                LocalDate.of(2026, 11, 25),
                new BigDecimal("125500.00"),
                "CERRADA",
                4L,
                "Alejandro Ramirez",
                1L,
                "Sabores sobre Ruedas",
                LocalDateTime.of(2026, 11, 25, 22, 0),
                null,
                null);
    }

    @Test
    void registrarCierreYReabrir_retornan201Y200() throws Exception {
        when(cierreCajaApiService.registrarCierre(any())).thenReturn(cierreEjemplo);
        when(cierreCajaApiService.reabrirCaja(any())).thenReturn(cierreEjemplo);

        String bodyCierre = """
                {
                  "cajeroId": 4,
                  "unidadVentaId": 1,
                  "fecha": "25-11-2026",
                  "montoRecaudado": 125500.00,
                  "esAdmin": false
                }
                """;

        String bodyReapertura = """
                {
                  "cajeroId": 4,
                  "unidadVentaId": 1,
                  "fecha": "25-11-2026",
                  "motivo": "Corrección de pedido omitido"
                }
                """;

        mockMvc.perform(post("/api/v1/cajas/cerrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyCierre))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/cajas/1"))
                .andExpect(jsonPath("$.estado").value("CERRADA"));

        mockMvc.perform(post("/api/v1/cajas/reabrir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyReapertura))
                .andExpect(status().isOk());
    }

    @Test
    void listarCierres_conMultiplesFiltros_retorna200() throws Exception {
        when(cierreCajaApiService.buscarHistorialFiltrado(
                eq(4L),
                eq(1L),
                eq("CERRADA"),
                eq(LocalDate.of(2026, 11, 1)),
                eq(LocalDate.of(2026, 11, 30)),
                eq(new BigDecimal("10000"))))
                .thenReturn(List.of(cierreEjemplo));

        mockMvc.perform(get("/api/v1/cajas")
                        .param("cajeroId", "4")
                        .param("unidadVentaId", "1")
                        .param("estado", "CERRADA")
                        .param("fechaDesde", "01-11-2026")
                        .param("fechaHasta", "30-11-2026")
                        .param("montoMinimo", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerPorIdYResumen_retornanEstadosEsperados() throws Exception {
        ResumenCajaResponseDTO resumen = new ResumenCajaResponseDTO(
                "cajero@epicentro.com",
                "Alejandro Ramirez",
                LocalDate.of(2026, 11, 25),
                new BigDecimal("125500.00"),
                1);

        when(cierreCajaApiService.obtenerPorId(1L)).thenReturn(cierreEjemplo);
        when(cierreCajaApiService.obtenerPorId(99L))
                .thenThrow(new NoSuchElementException("No existe el cierre con id 99."));
        when(cierreCajaApiService.obtenerResumenPorCajero("cajero@epicentro.com", LocalDate.of(2026, 11, 25)))
                .thenReturn(resumen);

        mockMvc.perform(get("/api/v1/cajas/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/cajas/99")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/cajas/resumen")
                        .param("emailCajero", "cajero@epicentro.com")
                        .param("fecha", "25-11-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recaudacionAcumulada").value(125500.00));
    }
}

