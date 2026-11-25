package com.example.hito2grupo3.controllers.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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

import com.example.hito2grupo3.dto.FestivalDTO;
import com.example.hito2grupo3.services.api.IFestivalApiService;

@ExtendWith(MockitoExtension.class)
class FestivalRestControllerTest {

    @Mock
    private IFestivalApiService festivalApiService;

    private MockMvc mockMvc;
    private FestivalDTO festivalEjemplo;

    @BeforeEach
    void setUp() {
        FestivalRestController controller = new FestivalRestController(festivalApiService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        festivalEjemplo = new FestivalDTO(
                1L,
                "Epicentro Gourmet Primavera",
                "Primavera",
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 15));
    }

    @Test
    void crearFestival_retorna201YLocation() throws Exception {
        when(festivalApiService.crear(any())).thenReturn(festivalEjemplo);

        String body = """
                {
                  "nombre": "Epicentro Gourmet Primavera",
                  "temporada": "Primavera",
                  "fechaInicio": "2026-11-01",
                  "fechaFin": "2026-11-15"
                }
                """;

        mockMvc.perform(post("/api/v1/festivales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/festivales/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Epicentro Gourmet Primavera"))
                .andExpect(jsonPath("$.temporada").value("Primavera"));
    }

    @Test
    void crearFestival_fechasInvertidas_retorna400() throws Exception {
        String body = """
                {
                  "nombre": "Festival Inválido",
                  "temporada": "Primavera",
                  "fechaInicio": "2026-11-15",
                  "fechaFin": "2026-11-01"
                }
                """;

        mockMvc.perform(post("/api/v1/festivales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void listarFestivales_conMultiplesFiltros_retorna200() throws Exception {
        when(festivalApiService.buscarFiltrados(
                eq("Epicentro"),
                eq("Primavera"),
                eq(true),
                eq("En curso"),
                eq(LocalDate.of(2026, 11, 1)),
                eq(LocalDate.of(2026, 11, 30)),
                eq(2L)))
                .thenReturn(List.of(festivalEjemplo));

        mockMvc.perform(get("/api/v1/festivales")
                        .param("nombre", "Epicentro")
                        .param("temporada", "Primavera")
                        .param("activo", "true")
                        .param("estado", "En curso")
                        .param("fechaDesde", "01-11-2026")
                        .param("fechaHasta", "30-11-2026")
                        .param("unidadVentaId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].temporada").value("Primavera"));
    }

    @Test
    void obtenerPorId_noExistente_retorna404() throws Exception {
        when(festivalApiService.obtenerPorId(99L))
                .thenThrow(new NoSuchElementException("No existe el festival con id 99."));

        mockMvc.perform(get("/api/v1/festivales/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void actualizarBajaReactivarYUnidades_retorna200() throws Exception {
        when(festivalApiService.actualizar(eq(1L), any())).thenReturn(festivalEjemplo);
        when(festivalApiService.darDeBaja(1L)).thenReturn(festivalEjemplo);
        when(festivalApiService.reactivar(1L)).thenReturn(festivalEjemplo);
        when(festivalApiService.habilitarUnidad(1L, 5L)).thenReturn(festivalEjemplo);
        when(festivalApiService.desasociarUnidad(1L, 5L)).thenReturn(festivalEjemplo);

        String body = """
                {
                  "nombre": "Epicentro Gourmet Primavera",
                  "temporada": "Primavera",
                  "fechaInicio": "2026-11-01",
                  "fechaFin": "2026-11-15"
                }
                """;

        mockMvc.perform(put("/api/v1/festivales/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/festivales/1/baja")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/festivales/1/reactivar")).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/festivales/1/unidades/5")).andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/festivales/1/unidades/5")).andExpect(status().isOk());

        verify(festivalApiService).desasociarUnidad(1L, 5L);
    }
}
