package com.example.hito2grupo3.controllers.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

import com.example.hito2grupo3.dto.CategoriaCocinaDTO;
import com.example.hito2grupo3.services.api.ICategoriaCocinaApiService;

@ExtendWith(MockitoExtension.class)
class CategoriaCocinaRestControllerTest {

    @Mock
    private ICategoriaCocinaApiService categoriaApiService;

    private MockMvc mockMvc;
    private CategoriaCocinaDTO categoriaEjemplo;

    @BeforeEach
    void setUp() {
        CategoriaCocinaRestController controller = new CategoriaCocinaRestController(categoriaApiService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        categoriaEjemplo = new CategoriaCocinaDTO(
                1L,
                "Maestro Parrillero",
                new BigDecimal("180000.00"),
                true,
                3L);
    }

    @Test
    void crearCategoria_retorna201() throws Exception {
        when(categoriaApiService.crear(any())).thenReturn(categoriaEjemplo);

        String body = """
                {
                  "nombre": "Maestro Parrillero",
                  "plusFijo": 180000.00
                }
                """;

        mockMvc.perform(post("/api/v1/categorias-cocina")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/categorias-cocina/1"))
                .andExpect(jsonPath("$.nombre").value("Maestro Parrillero"));
    }

    @Test
    void crearCategoria_plusNegativo_retorna400() throws Exception {
        String body = """
                {
                  "nombre": "Ayudante",
                  "plusFijo": -100.00
                }
                """;

        mockMvc.perform(post("/api/v1/categorias-cocina")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void listarCategorias_conCuatroFiltros_retorna200() throws Exception {
        when(categoriaApiService.buscarFiltradas(
                eq("Maestro"),
                eq(true),
                eq(new BigDecimal("50000")),
                eq(new BigDecimal("200000"))))
                .thenReturn(List.of(categoriaEjemplo));

        mockMvc.perform(get("/api/v1/categorias-cocina")
                        .param("nombre", "Maestro")
                        .param("activa", "true")
                        .param("plusMinimo", "50000")
                        .param("plusMaximo", "200000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerYOperacionesSobreCategoria_retornanEstadosEsperados() throws Exception {
        when(categoriaApiService.obtenerPorId(1L)).thenReturn(categoriaEjemplo);
        when(categoriaApiService.actualizar(eq(1L), any())).thenReturn(categoriaEjemplo);
        when(categoriaApiService.darDeBaja(1L)).thenReturn(categoriaEjemplo);
        when(categoriaApiService.reactivar(1L)).thenReturn(categoriaEjemplo);
        when(categoriaApiService.asignarCocinero(1L, 6L)).thenReturn(categoriaEjemplo);
        when(categoriaApiService.obtenerPorId(99L))
                .thenThrow(new NoSuchElementException("No existe la categoría con id 99."));

        String body = """
                {
                  "nombre": "Maestro Parrillero",
                  "plusFijo": 180000.00
                }
                """;

        mockMvc.perform(get("/api/v1/categorias-cocina/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/categorias-cocina/99")).andExpect(status().isNotFound());
        mockMvc.perform(put("/api/v1/categorias-cocina/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/categorias-cocina/1/baja")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/categorias-cocina/1/reactivar")).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/categorias-cocina/1/cocineros/6")).andExpect(status().isOk());
    }
}
